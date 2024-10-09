package org.example

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.Text
import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.xml.parsers.DocumentBuilder

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun main() {
    val ficheroEntrada = Paths.get("src/main/resources/empleados.csv")
    val ficheroSalidaXML = Paths.get("src/main/resources/empleadosGenerados.xml")

    // 1.LLAMADA A FUNCION PARA LECTURA DE ARCHIVO
    println("Leido fichero")
    val empleadoDiccionario = leerFichero(ficheroEntrada)
    empleadoDiccionario.forEach { println(it) }

    //2.LLAMADA A FUNCION PARA LA GENERACION DEL XML
    println("Creacion de fichero XML")
    empleadosXML(empleadoDiccionario, ficheroSalidaXML)

    //3.MODIICACIONES DE UN NODO
    do {
        println("Quieres modificar el salario de  algun usuario?")
        println("[1] Si\n[2]No")
        val decision: Int = readLine()!!.toInt()
        if (decision == 1) {
            println("ID de usuario?")
            val idEmpleado: String = readLine().toString()
            println("Cuanto es el salario?")
            val nuevoSalario: Double = readLine()!!.toDouble()
            modificarSalarioEmpleado(ficheroSalidaXML, idEmpleado, nuevoSalario)
        }
    } while (decision != 2)


    //4.LECTURA DEL ARCHIVO XML
    val lecturaXML = Paths.get("src/main/resources/empleadosGenerados.xml")
    val empleadosDiccionario2 = lecturaFicheroNuevo(lecturaXML)
    empleadosDiccionario2.forEach { println(it) }

}


fun leerFichero(ruta: Path): MutableMap<String, MutableList<String>> {
    val diccionario: MutableMap<String, MutableList<String>> = mutableMapOf()

    val br: BufferedReader = Files.newBufferedReader(ruta)
    val lineasarchivos = mutableListOf<List<String>>()
    br.use {
        it.readLine()
        it.forEachLine { line ->
            val lineaSpliteada: List<String> = line.split(",")
            lineasarchivos.add(lineaSpliteada)
            diccionario[lineaSpliteada[0]] = mutableListOf(
                lineaSpliteada[1],
                lineaSpliteada[2],
                lineaSpliteada[3]
            )
        }
    }


    return diccionario
}


fun empleadosXML(diccionario: MutableMap<String, MutableList<String>>, rutaSalida: Path) {

    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()
    val imp = builder.domImplementation

    val document: Document = imp.createDocument(null, "empleados", null)

    diccionario.forEach { (id, datos) ->
        val empleado: Element = document.createElement("empleado")
        empleado.setAttribute("id", id)


        // Crear el elemento apellido
        val apellido: Element = document.createElement("apellido")
        val textApellido: Text =
            document.createTextNode(datos[0]) //localizando la posicion del apellido por el enunciado
        apellido.appendChild(textApellido)
        empleado.appendChild(apellido)

        // Crear el elemento departamento
        val departamento: Element = document.createElement("departamento")
        val textDepartamento: Text =
            document.createTextNode(datos[1]) //localizando la posicion del departamento por el enunciado
        departamento.appendChild(textDepartamento)
        empleado.appendChild(departamento)

        // Crear el elemento salario
        val salario: Element = document.createElement("salario")
        val textSalario: Text =
            document.createTextNode(datos[2]) //localizando la posicion del salario por el enunciado
        salario.appendChild(textSalario)
        empleado.appendChild(salario)

        document.documentElement?.appendChild(empleado)
    }

    val source = DOMSource(document)
    val result = StreamResult(rutaSalida.toFile())
    val transformer = TransformerFactory.newInstance().newTransformer()

    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.transform(source, result)

}

fun modificarSalarioEmpleado(ficheroSalida: Path, idEmpleado: String, nuevoSalario: Double) {

    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()

    // Parseamos el documento XML
    val document: Document = builder.parse(ficheroSalida.toFile())
    val root: Element = document.documentElement
    root.normalize()

    //Obtenemos todos los nodos de los empleados
    val listaNodos: NodeList = root.getElementsByTagName("empleado")

    var empleadoModificado: Boolean = false

    for (i in 0 until listaNodos.length) {
        val nodo: Node = listaNodos.item(i)

        if (nodo.nodeType == Node.ELEMENT_NODE) {
            val nodoElemento: Element = nodo as Element

            val idActual: String = nodoElemento.getAttribute("id")

            if (idActual == idEmpleado) {
                val salarioNodo = nodoElemento.getElementsByTagName("salario").item(0)
                salarioNodo.textContent = nuevoSalario.toString()
                empleadoModificado = true
                break //todo
            }
        }
    }

    if (empleadoModificado) {// guardamos los cambios en el archivo XML
        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "no")// ponemos no para que no separe las cosas
        val source = DOMSource(document)
        val result = StreamResult(ficheroSalida.toFile())
        transformer.transform(source, result)
        println("El salario del empleado ${idEmpleado} ha sido cambiado a ${nuevoSalario}")

    } else {
        println("El empleado no se ha encontrado")
    }


}


fun lecturaFicheroNuevo(ficheroSalida: Path): MutableMap<String, MutableList<String>> {

    val diccionario: MutableMap<String, MutableList<String>> = mutableMapOf()

    val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()

    val db: DocumentBuilder = dbf.newDocumentBuilder()

    val document: Document = db.parse(ficheroSalida.toFile())

    val root: Element = document.documentElement

    root.normalize()

    val listadoNodos: NodeList = root.getElementsByTagName("empleado")

    for (i in 0 until listadoNodos.length) {

        val nodo: Node = listadoNodos.item(i)

        if (nodo.nodeType == Node.ELEMENT_NODE) {
            val nodoElemento: Element = nodo as Element

            val iD: String = nodoElemento.getAttribute("id")//preguntar a Diego
            val apellido: String = nodoElemento.getElementsByTagName("apellido").item(0).textContent
            val departamento: String = nodoElemento.getElementsByTagName("departamento").item(0).textContent
            val salario: String = nodoElemento.getElementsByTagName("salario").item(0).textContent


            diccionario[iD] = mutableListOf(apellido, departamento, salario)

        }
    }
    return diccionario
}
