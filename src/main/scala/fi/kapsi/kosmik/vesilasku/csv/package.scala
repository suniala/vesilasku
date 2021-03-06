package fi.kapsi.kosmik.vesilasku

import java.io.File

import scala.io.Source

package object csv {

  def fromFile(path: String, requiredColNames: List[String], separator: Char): Csv = {
    val file = new File(path)
    if (!(file.exists() && file.canRead)) {
      throw new IllegalArgumentException(s"can not read file $path")
    }

    val rawRows = Source.fromFile(file).getLines().toList

    val header = splitRow(rawRows.head, separator)
    assertColNames(requiredColNames, header)

    val rows = rawRows.tail.map(row => splitRow(row, separator))

    rows.foreach(row => {
      if (row.length != header.length) {
        throw new IllegalArgumentException("not all rows have an equal number of columns")
      }
    })

    new Csv(header, rows)
  }

  def toCsv(producer: CsvProducer, separator: Char = ';', newline: String = "\n"): String = {
    val colCount = producer.header().length

    (List(producer.header()) ++ producer.rows())
      .map(dataRow => {
        if (dataRow.length != colCount) {
          throw new IllegalArgumentException(s"expected all rows to have $colCount columns " +
            s"but found one with ${dataRow.length} columns")
        }
        dataRow
      })
      .map(dataRow => dataRow.mkString(separator.toString))
      .mkString(newline)
  }

  private def assertColNames(requiredColNames: List[String], header: List[String]): Unit = {
    requiredColNames.foreach(colName => {
      if (!header.contains(colName)) {
        throw new MissingColumnException(colName)
      }
    })
  }

  private def splitRow(row: String, separator: Char): List[String] = row.split(separator).map(_.trim).toList
}