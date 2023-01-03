import org.apache.sedona.core.formatMapper.shapefileParser.ShapefileReader
import org.apache.sedona.core.spatialRDD.SpatialRDD
import org.apache.sedona.core.utils.SedonaConf
import org.apache.sedona.sql.utils.{Adapter, SedonaSQLRegistrator}
import org.apache.sedona.viz.core.Serde.SedonaVizKryoRegistrator
import org.apache.sedona.viz.sql.utils.SedonaVizRegistrator
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel

object ExampleSedona extends App {

	/* setup */
	var sparkSession:SparkSession = SparkSession.builder()
		.master("local[*]")
		.appName("SedonaSQL-demo")
		.config("spark.serializer",classOf[KryoSerializer].getName)
		.config("spark.kryo.registrator", classOf[SedonaVizKryoRegistrator].getName)
		.getOrCreate()

	SedonaSQLRegistrator.registerAll(sparkSession)
  	SedonaVizRegistrator.registerAll(sparkSession)

	/* read files */
	val resourceFolder = System.getProperty("user.dir")+"/src/test/resources/"

	// get famous points from CSV
	var famousPoints = sparkSession.read.format("csv").option("delimiter", ",").option("header", "true").load(resourceFolder + "famous_attractions.csv")
	famousPoints.createOrReplaceTempView("famous_points")

	// country boundaries
	var countries = sparkSession.read.format("csv").option("delimiter", ":").option("header", "true").load(resourceFolder + "country_bounds.csv")
	countries.createOrReplaceTempView("countries")
	famousPoints.show()

	/* queries */
	calc_distances_SQL()
	calc_distances_GeoSpark()
	inUSA()

	/* calc distances */
	def calc_distances_SQL():Unit = {
		val x = -48.3576272
		val y = -15.7751884

		var distances = sparkSession.sql(f"""
			SELECT name,x,y,SQRT(
				POW(abs(x-($x)),2)+POW(abs(y-($y)),2)
			) as distance
			FROM famous_points
			ORDER BY distance ASC
			LIMIT 5
		""")
		distances.createOrReplaceTempView("smaller_distances1")
		distances.show(truncate=false,numRows=100)
	}

	def calc_distances_GeoSpark():Unit = {
		val myPosWKT = "POINT (-48.3576272 -15.7751884)"
		var distances = sparkSession.sql(s"""
			SELECT name,x,y,ST_Distance(
				ST_GeomFromWKT('$myPosWKT'), ST_POINT(x,y)
			) AS distance
			FROM famous_points
			ORDER BY distance ASC
			LIMIT 5
		""")
		distances.createOrReplaceTempView("smaller_distances2")
		distances.show(truncate=false,numRows=100)
	}

	// famous points in USA
	def inUSA():Unit = {
		var pointsin = sparkSession.sql(s"""
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
		""")
		pointsin.createOrReplaceTempView("pointsin")
		pointsin.show(truncate=false,numRows=100)
	}

	sparkSession.stop()
}
