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
import java.io.FileOutputStream
import java.io.FileWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


fun main() {

    //write
    val kv : Map<String, String> = mapOf(
        "XG_Public_OK" to "确定",
        "XG_Public_Cancel" to "取消"
        )
    var language = Language(language = "zh", kv = kv)
//    XmlFileHelper.write(File("${Common.path}values-${language.language}/string.xml"), language)

    //read
//    val lang = XmlFileHelper.read(File("${Common.path}values-${language.language}/string.xml"))


    //excel write
    ExcelHelper.write(File("${Common.path}i18n.xlsx"), language)

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
    //获取某一行
    operator fun Sheet.get(n: Int): Row = this.getRow(n) ?: this.createRow(n)
    operator fun Row.get(n: Int): Cell = this.getCell(n) ?: this.createCell(n, CellType.BLANK)
    operator fun Row.get(v: Any): Int{
        val cellIterator: Iterator<Cell> = this.cellIterator()
        var index = 0

        while (cellIterator.hasNext()) {
            val cell: Cell = cellIterator.next()
            index = cell.rowIndex
            // 获取单元格的值并与目标值比较
            val cellValue: String = cell.stringCellValue
            if (cellValue == v) {
                return cell.rowIndex
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
    }
    operator fun Sheet.get(x: Int, y: Int): Cell = this[x][y]
    operator fun Sheet.set(x: Int, y: Int, value: Any?){
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
    }

    override fun write(file: File, language: Language): File {
        //创建工作簿 + 工作表
        val book: Workbook = XSSFWorkbook()
        val sheet: Sheet = book.createSheet("language")

        val languageName: String = language.language
        val row:Row = sheet[0]
        val rowIndex = row[languageName]

        sheet[0,0] = "key"


        //创建表格
        var index: Int = 1
        for((k,v) in language.kv){
            sheet[index, 0] = k
            sheet[index, rowIndex] = v
            index++
        }

        // 保存到文件
        val fileOut = FileOutputStream(file.absolutePath, false)
        book.write(fileOut)
        fileOut.close()

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

