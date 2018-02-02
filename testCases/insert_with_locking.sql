locking some_table for write
locking another_table for access
insert into some_table
select *
from another_table
;
