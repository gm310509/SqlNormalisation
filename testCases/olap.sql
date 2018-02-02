/* A query to test OLAP functions*/
   select /* Windowed OLAP function to link rows */
    id1, id2, id3, id3 / 4, id2 / 3.14e6 'is' as annotation, min(val1) over (
    partition by id1, id2
    order by id3
    rows between 1 preceding AND 1 PRECEDING  -- get the previous row
   ) as prev_val1,
   val1,
   val1 - prev_val1 as delta
from compute_break_sample
order by 1,2,3
;                            


