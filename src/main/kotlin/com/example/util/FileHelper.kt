package com.example.util

import com.example.bean.Language
import com.example.common.Common
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.TransformerFactory.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


fun main() {

    val kv : Map<String, String> = mapOf(
        "XG_Public_OK" to "确定",
        "XG_Public_Cancel" to "取消"
        )
    var language = Language(language = "zh", kv = kv)

    XmlFileHelper().write(File("${Common.path}values-${language.language}/string.xml"), language)
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


class XmlFileHelper: FileOperationStrategy{


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
        for ((k, v) in language.kv){
            addNode(document, rootElement, k, v)
        }

        // 将文档写入文件
        val transformer = newInstance().newTransformer()
        transformer.transform(DOMSource(document), StreamResult(FileWriter(file)))


        return file
    }

    override fun read(file: File): Language {
        val language: Language = Language("", mapOf())

        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document: Document = documentBuilder.parse(file)
        document.documentElement.normalize()

        // 获取根节点
        val rootElement: Element = document.documentElement

        // 获取所有名为 "string" 的节点
        val nodeList: NodeList = rootElement.getElementsByTagName("string")

        for (i in 0 until nodeList.length) {
            val node: Node = nodeList.item(i)

            // 判断节点是否为 Element 类型
            if (node.nodeType == Node.ELEMENT_NODE) {
                val element = node as Element

                // 判断节点的属性是否为 "XG_Public_OK"
                if (element.getAttribute("name") == "XG_Public_OK") {
                    return language //need fix
                }
            }
        }

        // 如果未找到匹配的节点，则返回空字符串
        return language
    }

}


class StringsFileHelper: FileOperationStrategy{
    override fun write(file: File, language: Language): File {
        TODO("Not yet implemented")
    }

    override fun read(file: File): Language {
        TODO("Not yet implemented")
    }

}


class ExcelHelper: FileOperationStrategy{
    override fun write(file: File, language: Language): File {
        TODO("Not yet implemented")
    }

    override fun read(file: File): Language {
        TODO("Not yet implemented")
    }

}

