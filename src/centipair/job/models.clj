(ns centipair.job.models
  (:use korma.db
        centipair.core.db.connection
        centipair.core.contrib.mail
        centipair.core.contrib.time
        centipair.core.utilities.pagination)
  (:require [korma.core :as korma :refer [insert
                                          delete
                                          select
                                          where
                                          set-fields
                                          values
                                          fields
                                          offset
                                          limit
                                          defentity]]
            [centipair.core.contrib.time :as time]
            [validateur.validation :refer :all]
            [centipair.core.contrib.validator :as validator]
            [centipair.core.auth.user.models :as user-models]
            [clojure.string :refer [trim split]]))

(defentity job)
(defentity job_editor)
(defentity job_tag)


(def job-validator
  (validation-set
   (presence-of :job-title :message "Please enter a job title")
   (presence-of :job-location :message "Please enter the job location")
   (presence-of :job-how-to-apply :message "Please fill this field")
   (presence-of :job-company-name :message "Please fill this field")
   (presence-of :job-company-location :message "Please fill this field")))


(defn validate-job [params]
  (let [validation-result (job-validator params)]
    (if (valid? validation-result)
      true
      [false {:validation-result {:errors validation-result}}])))


(defn get-user-job [user-id job-id]
  (select job_editor (where {:user_account_id user-id
                             :job_id job-id})))

(defn job-editor? [request job-id]
  (let [job-id (:id (:params request))
        user-account (user-models/get-authenticated-user request)]    
    (if (nil? user-account)
      false
      (if (nil? (get-user-job (:user_account_id user-account) (Integer. job-id)))
        false
        true))))


(defn get-job
  [job-id]
  (select job (where {:job_id (Integer. job-id)})))


(defn job-exists? [job-id]
  (not (nil? (get-job job-id))))


(defn get-all-jobs [request]
  (let [params (:params request)
        user-account (user-models/get-authenticated-user request)
        offset-limit-params (offset-limit (:page params) (:per params))
        owned-jobs (select job_editor
                             (fields :job_id)
                             (where {:user_account_id (:user_account_id user-account)}))
        total (count owned-jobs)
        job-ids (reduce (fn [previous next] (conj previous (:job_id next))) [] owned-jobs)]
    
    (if (> total 0)
      {:result (select job (where {:job_id [in job-ids]})
                       (limit (:limit offset-limit-params))
                       (offset (:offset offset-limit-params)))
       :total total
       :page (if (nil? (:page params)) 0 (Integer. (:page params)))}
      {:result []
       :total 0
       :page 0})))


(defn get-job-list
  [request]
  (let [params (:params request)
        offset-limit-params (offset-limit (:page params) (:per params))
        jobs (select job
                     (fields :job_id
                             :job_title
                             :job_location
                             :job_type
                             :job_budget
                             :job_budget_interval)
                     (limit (:limit offset-limit-params))
                     (offset (:offset offset-limit-params)))
        ;;total (count jobs)
        page (if (nil? (:page params)) 0 (Integer. (:page params)))]
    {:result jobs
     ;;:total total
     :page page}))


(defn add-job-editor
  [job-id user-id]
  (insert job_editor (values
                      {:job_id job-id :user_account_id user-id})))


(defn add-tag
  [job-id tag]
  (if (not (nil? tag))
    (insert job_tag (values {:job_id (Integer. job-id) :job_tag_name tag}))))


(defn clean-tag
  [tag]
  (let [trimmed (trim tag)]
    (if (validator/has-value? trimmed)
      trimmed
      nil)))


(defn delete-job-tags
  [job-id]
  (delete job_tag (where {:job_id (Integer. job-id)})))


(defn create-job-tags
  [job-id tags]
  (delete-job-tags job-id)
  (let  [tags (split tags #",")
         cleaned-tags (map clean-tag tags)]
    (doseq [tag cleaned-tags]
      (add-tag job-id tag))))

(defn clean-job-budget
  [params]
  (if (= "quote" (:job-budget-interval params))
    (bigdec 0)
    (bigdec (:job-budget params))))

(defn create-job
  [params]
  (let [new-job (insert job (values 
                             {:job_title (:job-title params)
                              :job_type (:job-type params)
                              :job_location (:job-location params)
                              :job_description (:job-description params)
                              :job_how_to_apply (:job-how-to-apply params)
                              :job_who_can_apply (:job-who-can-apply params)
                              :job_company_name (:job-company-name params)
                              :job_company_location (:job-company-location params)
                              :job_budget (clean-job-budget params)
                              :job_budget_interval (:job-budget-interval params)
                              :job_tags (:job-tags params)
                              :job_created_date (time/sql-time-now)
                              :job_updated_date (time/sql-time-now)
                              :job_expiry_date (time/set-expire-days 183) ;;six months
                              }))]
    (do
      (add-job-editor (:job_id new-job) (:user-account-id params))
      (create-job-tags (:job_id new-job) (:job-tags params)))
    {:job_id (:job_id new-job)}))


(defn update-job
  [params]
  (let [updated-job (korma/update job
                            (set-fields
                             {:job_title (:job-title params)
                              :job_type (:job-type params)
                              :job_location (:job-location params)
                              :job_description (:job-description params)
                              :job_how_to_apply (:job-how-to-apply params)
                              :job_who_can_apply (:job-who-can-apply params)
                              :job_company_name (:job-company-name params)
                              :job_company_location (:job-company-location params)
                              :job_budget (clean-job-budget params)
                              :job_budget_interval (:job-budget-interval params)
                              :job_tags (:job-tags params)
                              :job_updated_date (time/sql-time-now)
                              })
                            (where {:job_id (Integer. (params :job-id))}))]
    (do
      (create-job-tags (:job-id params) (:job-tags params)))
    {:job_id (:job-id params)}))


(defn publish-job [job-id]
  (korma/update job (set-fields
               {:job_active true
                :job_published_date (time/sql-time-now)
                })
          (where {:job_id (Integer. job-id)})))


(defn delete-job [job-id]
  (delete job (where {:job_id (Integer. job-id)})))


(defn save-job [params]
  (if (validator/has-value? (:job-id params))
    (update-job params)
    (create-job params)))
