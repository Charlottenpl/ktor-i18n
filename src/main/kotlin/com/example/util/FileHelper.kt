package com.example.util

import com.example.bean.Language
import com.example.common.Common
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


fun main() {

    //write
    val zh : Map<String, String> = mapOf(
        "XG_Public_OK" to "确定",
        "XG_Public_Cancel" to "取消"
        )
    var zh_language = Language(language = "zh", kv = zh)
    val en : Map<String, String> = mapOf(
        "XG_Public_OK" to "ok",
        "XG_Public_Cancel" to "cancel"
    )
    var en_language = Language(language = "en", kv = en)
//    XmlFileHelper.write(File("${Common.path}values-${language.language}/string.xml"), language)

    //read
//    val lang = XmlFileHelper.read(File("${Common.path}values-${language.language}/string.xml"))


    //excel write
    ExcelHelper.write(File("${Common.path}i18n.xlsx"), en_language)

}

enum class FileType{
    xml,
    strings,
    excel
}

/**
 * 文件操作策略接口
 */
interface FileOperationStrategy {
    fun write(file: File, language: Language): File;
    fun read(file: File): Language;
}

object XmlFileHelper: FileOperationStrategy{
    private fun addNode(document: Document, root: Element, key: String, value: String){
        // 创建string节点
        val node: Element = document.createElement("string")
        node.setAttribute("name", key)
        node.textContent = value
        root.appendChild(node)
    }
    override fun write(file: File, language: Language): File {
        //将language中存储的信息写入file，并返回一个File

        // 如果文件不存在，则创建新文件
        if (!file.parentFile.exists()){
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }

        // 创建XML文档
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()

        // 创建根节点
        val rootElement: Element = document.createElement("resources")
        document.appendChild(rootElement)

        // 创建string节点
        for ((k, v) in language.kv?: mapOf()){
            addNode(document, rootElement, k, v)
        }

        // 将文档写入文件
        val transformer = newInstance().newTransformer()
        transformer.transform(DOMSource(document), StreamResult(FileWriter(file)))


        return file
    }
    override fun read(file: File): Language {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document: Document = documentBuilder.parse(file)
        document.documentElement.normalize()

        // 获取根节点
        val rootElement: Element = document.documentElement

        // 获取所有名为 "string" 的节点
        val nodeList: NodeList = rootElement.getElementsByTagName("string")

        val language: Language = Language()
        val parentFile = file.parentFile.nameWithoutExtension
        language.language = parentFile.substringAfterLast("values-")


        //获取map
        val kv : MutableMap<String, String> = mutableMapOf()
        for (i in 0 until nodeList.length) {
            val node: Node = nodeList.item(i)

            // 判断节点是否为 Element 类型
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element
                val key: String = element.getAttribute("name")
                val value = element.textContent

                if (key.isNotBlank())
                    kv[key] = value
            }
        }

        language.kv = kv

        // 如果未找到匹配的节点，则返回空字符串
        return language
    }
}

object StringsFileHelper: FileOperationStrategy{
    override fun write(file: File, language: Language): File {
        TODO("Not yet implemented")
    }

    override fun read(file: File): Language {
        TODO("Not yet implemented")
    }

}










object ExcelHelper: FileOperationStrategy{

    operator fun Sheet.get(n: Int): Row{
        println("sheet.get $n")
        return this.getRow(n) ?: this.createRow(n)
    } //获取第n行，如果没有则新建
    operator fun Row.get(n: Int): Cell{
        println("Row.get $n")
        return this.getCell(n) ?: this.createCell(n, CellType.BLANK)
    } //获取某一行第n个cell，如果没有则新建
    operator fun Sheet.get(x: Int, y: Int): Cell {
        println("sheet.get($x , $y)")
        val row = this[y]
        val cell = row[x]
        return cell
    }  //获取表中坐标为（x，y）的cell的值  ---重写
    operator fun Row.get(v: Any): Int{
        println("Row.get $v")
        var index = 0

        for (i in 0 until  this.lastCellNum){
            index = i
            val cell = this.getCell(i)
            // 获取单元格的值并与目标值比较
            val cellValue: String = cell.stringCellValue
            if (cellValue == v) {
                return cell.columnIndex
            }
        }

        // 如果循环结束仍未找到目标值，则返回 false
        val newCell = this.createCell(++index)
        when (v) {
            // 判断value的数据类型，然后用转换之后填入
            is String -> newCell.setCellValue(v)
            is Int -> newCell.setCellValue(v.toString())
            is Double -> newCell.setCellValue(v.toString())
            else -> throw IllegalArgumentException("数据类型不支持")
        }
        return index
    }  //获取某一行值为v的cell的index，如果没有则新建
    operator fun Sheet.set(x: Int, y: Int, value: Any?){
        println("Sheet.set $x, $y, $value")
        value?.let {
            // 所有的字段都设置成文本字段
            val textStyle = this.workbook.createCellStyle()
            textStyle.dataFormat = this.workbook.createDataFormat().getFormat("@")
            this.setDefaultColumnStyle(x, textStyle)
            val cell = this[x, y]
            when (value) {
                // 判断value的数据类型，然后用转换之后填入
                is String -> cell.setCellValue(value)
                is Int -> cell.setCellValue(value.toString())
                is Double -> cell.setCellValue(value.toString())
                else -> throw IllegalArgumentException("数据类型不支持")
            }
        }
    } //设置表中坐标为（x，y）的cell的值为value
    operator fun Sheet.get(x: Int, value: String): Int{
        println("Sheet.get $x, $value")
        var cellNumber: Int = this.lastRowNum //这里返回的是下标。。
        for (i in 0 until cellNumber + 1){
            val cv:String = this[x,i].stringCellValue
            if (cv == value){
                return i
            }

            if (cv.isEmpty()){
                this[x, i] = value
                return i
            }
        }

        this[x, ++cellNumber] = value
        return cellNumber
    }  //获取sheet中第x列值为value的行数，如果没有则新建

    operator fun Sheet.get(value: String, y: Int): Int{
        println("Sheet.get $y, $value")
        val row: Row = this[y]

        for (i in 0 until row.lastCellNum){
            val cv:String = row[i].stringCellValue
            if (cv == value){
                return i
            }

            if (cv.isEmpty()){
                row[i].setCellValue(value)
                return i
            }
        }

        row[row.lastCellNum+1].setCellValue(value)
        return row.lastCellNum.toInt()
    }  //获取sheet中第x列值为value的行数，如果没有则新建
    override fun write(file: File, language: Language): File {
        //创建工作簿 + 工作表
        val book: Workbook = if (file.exists()){
            XSSFWorkbook(FileInputStream(file.path))
        }else {
            XSSFWorkbook()
        }

        val sheet: Sheet = book.getSheet("language") ?:book.createSheet("language")

        val languageName: String = language.language
        val row:Row = sheet[0]
        val xIndex = row[languageName]

        sheet[0,0] = "key"

        //创建表格
        for((k,v) in language.kv){
            //确定key的行数
            sheet[xIndex, sheet[0, k]] = v
        }

        // 保存修改后的 Excel 文件
        val outputStream = FileOutputStream(file)
        book.write(outputStream)
        outputStream.flush()
        outputStream.close()

        // 关闭工作簿
        book.close()
        println("Excel file created successfully.")

        return file
    }

    override fun read(file: File): Language {
        TODO("Not yet implemented")
    }

}


//operator fun Sheet.add(x: Int, y: Int, value: String): Sheet =
//    {
//
//    }

