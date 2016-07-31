import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.bitbucket.eunjeon.seunjeon.Analyzer
import scala.collection.mutable.ListBuffer

object SparkApp {

  def main(args: Array[String]) {

    var jsonFileInfo = "input/input2.json"

    val conf = new SparkConf()

    // if you want to run at spark cluster, put argument as "cluster" when you run this  
    if (args.isEmpty) {
      conf.setAppName("SparkApp").setMaster("local")
      println(">>>>> IDE development mode >>>>>")

    } else if (args(0).equals("cluster")) {
      println(">>>>> cluster mode >>>>>")
      if (!args(1).isEmpty()) {
        jsonFileInfo = args(1)
      }
    }

    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)

    var no = 1

    val outputListBuffer: ListBuffer[String] = ListBuffer()
    val outputList: List[String] = List()

    val jsonRDD = sc.wholeTextFiles(jsonFileInfo).map(x => x._2)
    val preparedJson = sqlContext.read.json(jsonRDD)
    preparedJson.printSchema()
    preparedJson.registerTempTable("blogContents")

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
      })
  }
}
