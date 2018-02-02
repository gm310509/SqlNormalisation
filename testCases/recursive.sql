

/* TD 15.00 recursive query. */
with RECURSIVE temp as ( /* Recursive social network query. */
     select         R.user_id, C.other_user_id (integer) as other_user_id, 1 (integer) as lvl
     from social_user as R join social_butterfly as C on
         R.user_id = C.user_id
     union all
     select D.user_id, I.other_user_id, lvl + 1
     from temp as D join social_butterfly as I on
         D.other_user_id = I.user_id
     where lvl < 10
)
select T.user_id,
--    U.user_name,
--    'knows',
    T.other_user_id,
--    K.user_name,
    lvl as degrees_of_separation
from temp T
--join social_user U on
--        T.user_id = U.user_id
--    Join social_user K on
--        T.other_user_id = K.user_id order by 1, 2 


