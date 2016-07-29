import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions.get_json_object

import play.api.libs.json._
import org.bitbucket.eunjeon.seunjeon.Analyzer
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions;
import java.lang.Boolean

object SparkApp {

  def main(args: Array[String]) {

    // if you want to run at spark cluster, put argument as "cluster" when you run this  
    if (args.isEmpty) {

      val conf = new SparkConf()
        .setAppName("SparkApp")
        .setMaster("local")

      val sc = new SparkContext(conf)
      val sqlContext = new SQLContext(sc)

      var no = 1
      val outputListBuffer: ListBuffer[String] = ListBuffer()
      val outputList: List[String] = List()

      println(">>>>> IDE development mode >>>>>")

      val jsonRDD = sc.wholeTextFiles("input/input2.json").map(x => x._2)
      val preparedJson = sqlContext.read.json(jsonRDD)
      preparedJson.printSchema()
      preparedJson.registerTempTable("blogContents")

      //      inputJson.show()
      val qeuryResult = sqlContext.sql("SELECT contents, url FROM blogContents")
      println("count: " + qeuryResult.count())

      qeuryResult
        .map(t => ("contents: " + t.getAs[String](0), t.getAs[String](1)))
        .collect()
        .foreach(z => {
          println(">>>>> start parsing >>>>>")
          var outputListBuffer2 = new ListBuffer[String]()
          no = 1
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
                val wordType = specificWordRemover(orgStrArray(0), ",").trim()
                val wordValue = specificWordRemover(orgStrArray(3), ",").trim()
                if (!specificWordMatchResult(wordType, "SY") && !specificWordMatchResult(wordType, "UNKNOWN")
                  && !specificWordMatchResult(wordType, "SL") && !specificWordMatchResult(wordType, "SC")) {
                  //                  println(z._2 + "," + no + "," + wordType + "," + wordValue)
                  outputListBuffer += (z._2 + "," + no.toString() + "," + wordType + "," + wordValue)
                  outputListBuffer2 += (z._2 + "," + no.toString() + "," + wordType + "," + wordValue)
                  no += 1
                }
              }
            })
          val outputResult = outputListBuffer2.toList
          outputResult.foreach(println)
          //          sc.parallelize(outputResult).saveAsTextFile("output")
        })

    } else if (args(0).equals("cluster")) {

      val conf = new SparkConf()

      val sc = new SparkContext(conf)
      val sqlContext = new SQLContext(sc)

      var no = 1
      val outputListBuffer: ListBuffer[String] = ListBuffer()
      var outputListBuffer2 = new ListBuffer[String]()

      println(">>>>> cluster mode >>>>>")
      val jsonRDD = sc.wholeTextFiles("input/input2.json").map(x => x._2)
      val preparedJson = sqlContext.read.json(jsonRDD)
      preparedJson.printSchema()
      preparedJson.registerTempTable("blogContents")
      
//      val inputJson = sqlContext.jsonFile("/home/hadoop/temp/input.json")
//      inputJson.printSchema()
//      inputJson.registerTempTable("inputJson")
      val qeuryResult = sqlContext.sql("SELECT contents FROM blogContents")
      val contents = qeuryResult
        .map(t => "contents: " + t.getAs[String](0))
        .collect()
        .foreach(z => {
          Analyzer
            .parse(z)
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
                val wordType = specificWordRemover(orgStrArray(0), ",").trim()
                val wordValue = specificWordRemover(orgStrArray(3), ",").trim()
                if (!specificWordMatchResult(wordType, "SY") && !specificWordMatchResult(wordType, "UNKNOWN")) {
                  println(no + "," + wordType + "," + wordValue)
                  no += 1
                }
              }
            })
        })
    }

  }
}
