package com.example.util

import com.example.bean.Language
import com.example.common.Common
import org.apache.commons.codec.language.bm.Lang
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
    val zh : HashMap<String, String> = HashMap()
    zh["XG_Public_OK"] = "确定"
    zh["XG_Public_Cancel"] = "取消"
    var zh_language = Language(language = "zh", kv = zh)
    val en : HashMap<String, String> = HashMap()
    en["XG_Public_OK"] = "ok"
    en["XG_Public_Cancel"] = "cancel"
    var en_language = Language(language = "en", kv = en)
//    XmlFileHelper.write(File("${Common.path}values-${language.language}/string.xml"), language)

    //read
//    val lang = XmlFileHelper.read(File("${Common.path}values-${language.language}/string.xml"))


    //excel write
//    ExcelHelper.write(File("${Common.path}i18n.xlsx"), listOf(en_language, zh_language))

    val list = ExcelHelper.read(File("${Common.path}i18n.xlsx"))
    XmlFileHelper.write(File(Common.path), list)


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
    fun write(file: File, language: List<Language>): File;
    fun read(file: File): List<Language>;
}

object XmlFileHelper: FileOperationStrategy{
    private fun addNode(document: Document, root: Element, key: String, value: String){
        // 创建string节点
        val node: Element = document.createElement("string")
        node.setAttribute("name", key)
        node.textContent = value
        root.appendChild(node)
    }

    private fun getXMLList(directory: File):List<File>{
        // 确保目标文件夹存在
        if (!directory.exists() || !directory.isDirectory) {
            throw IllegalArgumentException("Invalid directory: ${directory.path}")
        }

        // 获取所有符合 "value-xxx" 格式的文件夹
        val xmlFiles = directory.listFiles { file ->
            file.isDirectory && file.name.startsWith("value-")
        }

        if (xmlFiles != null && xmlFiles.isNotEmpty()) {
            return xmlFiles.asList()
        } else {
            throw NoSuchElementException("No matching directories found.")
        }
    }

    override fun write(file: File, language: List<Language>): File {
        //将language中存储的信息写入file，并返回一个File

        // 创建父文件夹
        if (!file.exists()){
            file.mkdirs()
        }

        // 循环language
        for (lang in language){
            // directory_name
            val directory = File(file, "value-${lang.language}")
            if (!directory.exists()){
                directory.mkdirs()
            }

            // create xml
            val xmlFile = File(directory, "string.xml")
            if (!xmlFile.exists()){
                xmlFile.createNewFile()
            }

            // ----------------------------------------------------------
            // 创建XML文档
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()

            // 创建根节点
            val rootElement: Element = document.createElement("resources")
            document.appendChild(rootElement)

            // 创建string节点
            for ((k, v) in lang.kv?: mapOf()){
                addNode(document, rootElement, k, v)
            }

            // 将文档写入文件
            val transformer = newInstance().newTransformer()
            transformer.transform(DOMSource(document), StreamResult(FileWriter(xmlFile)))
        }

        return file
    }
    override fun read(file: File): List<Language> {
        // 获取父目录下所有符合格式的文件夹
        val xmlFiles = getXMLList(file)
        val array: MutableList<Language> = mutableListOf() //result

        for (xmlFile in xmlFiles){

            //获取string.xml文件
            val xml = File(xmlFile, "string.xml")
            if (!xml.exists()){
                continue
            }

            val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val document: Document = documentBuilder.parse(file)
            document.documentElement.normalize()

            // 获取根节点
            val rootElement: Element = document.documentElement

            // 获取所有名为 "string" 的节点
            val nodeList: NodeList = rootElement.getElementsByTagName("string")

            val language = Language()
            language.language = xmlFile.nameWithoutExtension.substringAfterLast("values-")


            //获取map
            val kv : HashMap<String, String> = HashMap()
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
            array.add(language)
        }

        return array
    }
}

object StringsFileHelper: FileOperationStrategy{
    override fun write(file: File, language: List<Language>): File {
        TODO("Not yet implemented")
    }

    override fun read(file: File): List<Language> {
        TODO("Not yet implemented")
    }

}










object ExcelHelper: FileOperationStrategy{
    override fun write(file: File, language: List<Language>): File {
        //创建工作簿 + 工作表
        val book: Workbook = if (file.exists()){
            XSSFWorkbook(FileInputStream(file.path))
        }else {
            XSSFWorkbook()
        }

        val sheet: Sheet = book.getSheet("language") ?:book.createSheet("language")
        sheet[0,0] = "key"

        for (lang in language){
            val languageName: String = lang.language
            val row:Row = sheet[0]
            val xIndex = row[languageName]

            //更新表格
            for((k,v) in lang.kv){
                //确定key的行数
                sheet[xIndex, sheet[0, k]] = v
            }
        }

        // 保存修改后的 Excel 文件
        if (!file.parentFile.exists()){
            file.parentFile.mkdirs()
        }
        if (!file.exists()){
            file.createNewFile()
        }
        val outputStream = FileOutputStream(file)
        book.write(outputStream)
        outputStream.flush()
        outputStream.close()

        // 关闭工作簿
        book.close()
        println("Excel file created successfully.")

        return file
    }

    override fun read(file: File): List<Language> {
        //打开excel
        if (!file.exists()){
            throw NoSuchElementException("No such file found.")
        }

        val book: Workbook = XSSFWorkbook(FileInputStream(file.path))
        val sheet: Sheet = book.getSheet("language") ?:book.createSheet("language")

        //读取language - zh
        val list: ArrayList<Language> = ArrayList()
        val indexList: Array<Language?> = Array<Language?>(sheet[0].physicalNumberOfCells){null}


        // 判断sheet是否为空
        val rowNum = sheet.physicalNumberOfRows
        val cellNum = if (rowNum > 0) sheet.getRow(0).physicalNumberOfCells else 0

        if (rowNum < 2 && cellNum < 2)
            return list

        for (i in 0 until sheet.physicalNumberOfRows){
            //写入language
            if (i == 0){
                for (cell in sheet[0]){
                    if (cell.columnIndex == 0)
                        continue

                    val language = Language()
                    language.language = cell.stringCellValue
                    language.kv = HashMap()
                    indexList[cell.columnIndex] = language
                    list.add(language)
                }
                continue
            }


            //写入kv
            val key = sheet[i][0].stringCellValue
            val row = sheet[i]
            for (j in 0 until row.physicalNumberOfCells){
                if (j == 0){
                    continue
                }

                val value = row[j].stringCellValue
                indexList[j]!!.kv[key] = value
            }
        }
        return list
    }

}


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
}  //获取sheet中第x列值为value的行数，如果

//operator fun List<Language>.set(index: Int, value: Language){
//    if (this.get(index))
//}