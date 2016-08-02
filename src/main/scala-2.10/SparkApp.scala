import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.types.{ StructType, StructField, StringType, IntegerType };
import org.apache.spark.sql.Row;
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd
import org.apache.spark.sql._

import org.bitbucket.eunjeon.seunjeon.Analyzer

import scala.collection.mutable.ListBuffer

object SparkApp {

  // schema of dataframe should be located outside of main 
  val resultSchema = StructType(StructField("url", StringType, true) ::
    StructField("seq", StringType, true) :: StructField("type", StringType, true) ::
    StructField("name", StringType, true) :: Nil)

  def main(args: Array[String]) {

    var readJsonFileInfo = "input/input2.json"
    var writeMapFileInfo = "/output/seungjeon-output4"
    var parquetInfo = "seunjeonResult.parquet"

    val conf = new SparkConf()

    // if you want to run at spark cluster, put argument as "cluster" when you run this  
    if (args.isEmpty) {
      conf.setAppName("SparkApp").setMaster("local")
      println(">>>>> IDE development mode >>>>>")

    } else if (args(0).equals("cluster")) {
      println(">>>>> cluster mode >>>>>")
      if (!args(1).isEmpty()) {
        readJsonFileInfo = args(1)
      }
      if (!args(2).isEmpty()) {
        parquetInfo = args(2)
      }
    }

    val sc = new SparkContext(conf)
    //    val sqlContext = new SQLContext(sc)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    val outputListBuffer: ListBuffer[String] = ListBuffer()
    val outputList: List[String] = List()

    val jsonRDD = sc.wholeTextFiles(readJsonFileInfo).map(x => x._2)
    val preparedJson = sqlContext.read.json(jsonRDD)
    preparedJson.printSchema()
    preparedJson.registerTempTable("blogContents")

    val qeuryResult = sqlContext.sql("SELECT contents, url FROM blogContents")
    println("count: " + qeuryResult.count())

    var outputResult: List[String] = List()
    var jobNo = 1
    //    var outputListBuffer = new ListBuffer[String]()

    qeuryResult
      .map(t => ("contents: " + t.getAs[String](0), t.getAs[String](1)))
      .collect()
      .foreach(z => {
        println(">>>>> start " + jobNo.toString() + "th parsing >>>>>")
        jobNo += 1
        var no = 1
        Analyzer
          .parse(z._1)
          .foreach(x => {
            def specificWordRemover(inputStr: String, specificWord: String): String =
              {
                inputStr.replaceAll(("[" + specificWord + "]"), "")
              }
            def specificWordMatchResult(inputStr: String, specificWord: String): Boolean = {
              val regex = specificWord.r
              regex.findFirstIn(inputStr).isDefined
            }
            val orgStr = x.morpheme.toString()
            if (specificWordMatchResult(orgStr, "WrappedArray")) {
              val orgStrArray = orgStr.split("WrappedArray\\(")(1).split("\\)")(0).split(",")
              if (orgStrArray.length >= 4) {
                val wordType = specificWordRemover(orgStrArray(0), ",").trim()
                val wordValue = specificWordRemover(orgStrArray(3), ",").trim()
                if (!specificWordMatchResult(wordType, "SY") && !specificWordMatchResult(wordType, "UNKNOWN")
                  && !specificWordMatchResult(wordType, "SL") && !specificWordMatchResult(wordType, "SC")) {
                  //                  println(z._2 + "," + no + "," + wordType + "," + wordValue)
                  outputListBuffer += (z._2 + "," + no.toString() + "," + wordType + "," + wordValue)
                  //                  outputListBuffer2 += (z._2 + "," + no.toString() + "," + wordType + "," + wordValue)
                  no += 1
                }
              }
            }
          })

        if (args.isEmpty) {

        } else if (args(0).equals("cluster")) {
          if ((jobNo % 101).equals(0)) {
            outputResult = outputListBuffer.toList
            val rowRDD = outputResult.map(_.split(",")).map(p => Row(p(0), p(1), p(2), p(3).trim))
            val filteredRDD = sc.parallelize(rowRDD)
            val seunjeonDataFrame = sqlContext.createDataFrame(filteredRDD, resultSchema)
            seunjeonDataFrame.registerTempTable("result")
            seunjeonDataFrame.select("url", "seq", "type", "name").write.format("parquet").mode(org.apache.spark.sql.SaveMode.Append).save(parquetInfo)
            outputListBuffer.clear()
          }
        }
      })

    if (args.isEmpty) {
      outputResult = outputListBuffer.toList
      val rowRDD = outputResult.map(_.split(",")).map(p => Row(p(0), p(1), p(2), p(3).trim))
      val filteredRDD = sc.parallelize(rowRDD)
      val seunjeonDataFrame = sqlContext.createDataFrame(filteredRDD, resultSchema)
      seunjeonDataFrame.registerTempTable("result")
      //      val results = sqlContext.sql("SELECT name, type FROM result WHERE type = 'NNP'")
      val results = sqlContext.sql("SELECT url, seq, name, type FROM result")
      results.map(t => ("url: " + t(0), "seq: " + t(1), "type: " + t(2), "name: " + t(3))).collect().foreach(println)

    } else if (args(0).equals("cluster")) {

      //      val outputRdd = sc.parallelize(mapResult.productIterator.toList)
      //      outputRdd.saveAsTextFile(writeMapFileInfo)

    }

  }

}
