SELECT meta().id,info[0].record_title, info[0].note_info FROM `__BUCKET_NAME__`
where type = 'Record' and (info[0].note_info is not null and info[0].note_info != "[\n]")
and (
    `user` = 'doctor_13671609763' or 
    `user` = 'doctor_18121226579' or
    `user` = 'doctor_13120800696' or
    `user` = 'doctor_13764561303' 
    ) 
order by `user`;