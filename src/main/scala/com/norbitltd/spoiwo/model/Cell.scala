package com.norbitltd.spoiwo.model

import java.util.{Calendar, Date}
import java.time.{LocalDate => JLocalDate, LocalDateTime => JLocalDateTime}
import org.joda.time.{LocalDate, DateTime}
import com.norbitltd.spoiwo.model.enums.CellStyleInheritance
import com.norbitltd.spoiwo.utils.JavaTimeApiConversions._

sealed class CellValueType[T]
object CellValueType {
  implicit object StringWitness         extends CellValueType[String]
  implicit object DoubleWitness         extends CellValueType[Double]
  implicit object BigDecimalWitness     extends CellValueType[BigDecimal]
  implicit object IntWitness            extends CellValueType[Int]
  implicit object LongWitness           extends CellValueType[Long]
  implicit object BooleanWitness        extends CellValueType[Boolean]
  implicit object DateWitness           extends CellValueType[Date]
  implicit object DateTimeWitness       extends CellValueType[DateTime]
  implicit object LocalDateWitness      extends CellValueType[LocalDate]
  implicit object CalendarWitness       extends CellValueType[Calendar]
  implicit object JLocalDateWitness     extends CellValueType[JLocalDate]
  implicit object JLocalDateTimeWitness extends CellValueType[JLocalDateTime]
}

object Cell {

  lazy val Empty = apply("")

  def apply[T : CellValueType](value : T, index : java.lang.Integer = null, style : CellStyle = null, styleInheritance : CellStyleInheritance = CellStyleInheritance.CellThenRowThenColumnThenSheet) : Cell = {
    val indexOption = Option(index).map(_.intValue)
    val styleOption = Option(style)
    value match {
      case v : String => if (v.startsWith("=")) {
        FormulaCell(v.drop(1), indexOption, styleOption, styleInheritance)
      } else if (v.contains("\n")) {
        StringCell(v, indexOption, styleOption.map(_.withWrapText), styleInheritance)
      } else {
        StringCell(v, indexOption, styleOption, styleInheritance)
      }
      case v : Double         => NumericCell(v, indexOption, styleOption, styleInheritance)
      case v : BigDecimal     => NumericCell(v.toDouble, indexOption, styleOption, styleInheritance)
      case v : Int            => NumericCell(v.toDouble, indexOption, styleOption, styleInheritance)
      case v : Long           => NumericCell(v.toDouble, indexOption, styleOption, styleInheritance)
      case v : Boolean        => BooleanCell(v, indexOption, styleOption, styleInheritance)
      case v : Date           => DateCell(v, indexOption, styleOption, styleInheritance)
      case v : DateTime       => DateCell(v.toDate, indexOption, styleOption, styleInheritance)
      case v : LocalDate      => DateCell(v.toDate, indexOption, styleOption, styleInheritance)
      case v : JLocalDate     => DateCell(v.toDate, indexOption, styleOption, styleInheritance)
      case v : JLocalDateTime => DateCell(v.toDate, indexOption, styleOption, styleInheritance)
      case v : Calendar       => CalendarCell(v, indexOption, styleOption, styleInheritance)
    }
  }
}

sealed trait Cell {

  val value : Any
  val index : Option[Int]
  val style : Option[CellStyle]
  val styleInheritance : CellStyleInheritance
  val format : Option[String] = style.map(_.dataFormat.map(_.formatString)).flatten.flatten

  protected def copyCell(value : Any = value, index : Option[Int] = index, style : Option[CellStyle] = style) : Cell

  protected def valueToString() : String

  override def toString = {
    val attributes = List(index.map("index=" + _), style.map("style=" + _)).flatten
    val attributesString = if(attributes.isEmpty) "" else " (" + attributes.mkString(", ") + ")"
    valueToString() + attributesString
  }

  def withIndex(index : Int) =
    copyCell(index = Option(index))

  def withoutIndex =
    copyCell(index = None)

  def withStyle(style : CellStyle) =
    copyCell(style = Option(style))

  def withoutStyle =
    copyCell(style = None)

  def withDefaultStyle(defaultStyle : Option[CellStyle]) : Cell = if(defaultStyle.isEmpty) {
    this
  } else if( style.isEmpty ) {
    withStyle(defaultStyle.get)
  } else {
    val mergedStyle = style.get.defaultWith(defaultStyle.get)
    withStyle(mergedStyle)
  }


}

case class StringCell private[model](value: String, index: Option[Int], style: Option[CellStyle], styleInheritance : CellStyleInheritance)
  extends Cell {
  def copyCell(value : Any = value, index : Option[Int] = index, style : Option[CellStyle] = style) : Cell =
    copy(value.asInstanceOf[String], index, style)

  protected def valueToString() = "\"" + value + "\""
}

case class FormulaCell private[model](value: String,  index: Option[Int], style: Option[CellStyle], styleInheritance : CellStyleInheritance)
  extends Cell {
  def copyCell(value : Any = value, index : Option[Int] = index, style : Option[CellStyle] = style) : Cell =
    copy(value.asInstanceOf[String], index, style)

  protected def valueToString() = s"<=$value>"
}

case class NumericCell private[model](value: Double, index: Option[Int], style: Option[CellStyle], styleInheritance : CellStyleInheritance)
  extends Cell {
  def copyCell(value : Any = value, index : Option[Int] = index, style : Option[CellStyle] = style) : Cell =
    copy(value.asInstanceOf[Double], index, style)

  protected def valueToString() = value.toString
}

case class BooleanCell private[model](value: Boolean, index: Option[Int], style: Option[CellStyle], styleInheritance : CellStyleInheritance)
  extends Cell {
  def copyCell(value : Any = value, index : Option[Int] = index, style : Option[CellStyle] = style) : Cell =
    copy(value.asInstanceOf[Boolean], index, style)

  protected def valueToString() = value.toString
}

case class DateCell private[model](value: Date, index: Option[Int], style: Option[CellStyle], styleInheritance : CellStyleInheritance)
  extends Cell {
  def copyCell(value : Any = value, index : Option[Int] = index, style : Option[CellStyle] = style) : Cell =
    copy(value.asInstanceOf[Date], index, style)

  protected def valueToString() = value.toString
}

case class CalendarCell private[model](value: Calendar, index: Option[Int], style: Option[CellStyle], styleInheritance : CellStyleInheritance)
  extends Cell {
  def copyCell(value : Any = value, index : Option[Int] = index, style : Option[CellStyle] = style) : Cell =
    copy(value.asInstanceOf[Calendar], index, style)

  protected def valueToString() = value.toString
}