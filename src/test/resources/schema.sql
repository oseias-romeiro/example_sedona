
/*
 * Calculate distance between my position and famous tourist points
 * my position (Brasilia - Brazil): 'POINT (-47.9341 -15.7792)'
*/

-- sql
SELECT name,x,y,SQRT(
    POW(abs(x-(-47.9341)),2)+POW(abs(y-(-15.7792)),2)
) as distance
FROM famous_points
ORDER BY distance ASC
LIMIT 5

-- spark sql
SELECT name,x,y,ST_Distance(
    ST_POINT(-47.9341, -15.7792), ST_POINT(x,y)
) AS distance
FROM famous_points
ORDER BY distance ASC
LIMIT 5

/*
 * Calculate wich famous tourist points is into EUA
 * EUA boundaries is datailed in WKT format into country_bounds.csv
*/

SELECT name,x,y
FROM famous_points
WHERE ST_Contains(
    ST_GeomFromWKT((
        SELECT wkt
        FROM countries
        WHERE countries.name == 'United States of America'
    )),
    st_point(x,y)
)
