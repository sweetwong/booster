package com.didiglobal.booster.cha.graph.dot

import com.didiglobal.booster.cha.graph.CallGraph
import com.didiglobal.booster.cha.graph.CallGraphRenderer
import com.didiglobal.booster.command.Command
import com.didiglobal.booster.command.CommandService
import com.didiglobal.booster.kotlinx.OS
import com.didiglobal.booster.kotlinx.RGB
import com.didiglobal.booster.kotlinx.execute
import com.didiglobal.booster.kotlinx.stderr
import com.didiglobal.booster.kotlinx.touch
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Represents the graph type
 *
 * @author johnsonlee
 */
sealed class DotGraph : CallGraphRenderer {

    object DIGRAPH : DotGraph() {

        override fun render(graph: CallGraph): CharSequence {
            return StringBuilder().apply {
                appendln("digraph \"${graph.title}\" {")
                appendln("    graph [bgcolor=\"transparent\",pad=\"0.555\"];")
                appendln("    node [color=\"#00BFC4\",fillcolor=\"#00BFC440\",fontcolor=\"#333333\",fontname=Helvetica,shape=box,style=filled];")
                appendln("    edge [fontname=Helvetica];")
                appendln("    rankdir = TB;")
                graph.nodes.joinTo(this, "\n    ", "    ", "\n") { node ->
                    val id = graph.title.substringAfterLast('.')
                    val color = RGB.valueOf(WebSafeColorPalette.random(0x000000, 0xffffff)) // except white color
                    "\"${if (node == CallGraph.ROOT) id else node.toPrettyString()}\" [color=\"#$color\",fillcolor=\"#${color}40\"];"

                }
                graph.joinTo(this, "\n    ", "    ", "\n") { edge ->
                    val id = graph.title.substringAfterLast('.')
                    val color = RGB.valueOf(WebSafeColorPalette.random(0x000000, 0xffffff)).toString() // except white color
                    val from = if (edge.from == CallGraph.ROOT) id else edge.from.toPrettyString()
                    val to = if (edge.to == CallGraph.ROOT) id else edge.to.toPrettyString()
                    "\"$from\" -> \"$to\" [color=\"#$color\",fontcolor=\"#$color\"];"

                }
                appendln("}")
            }
        }

    }

    fun visualize(graph: CallGraph, output: File, format: String = "png", dot: Command = CommandService.fromPath("dot${OS.executableSuffix}")) {
        output.touch().writeText(render(graph).toString())
        dot.location.file.let(::File).takeIf(File::exists)?.let {
            "${it.canonicalPath} -T${format} -O ${output.canonicalPath}".also(::println).execute()
        }?.let { p ->
            p.waitFor()
            if (p.exitValue() != 0) {
                throw IOException(p.stderr)
            }
        } ?: throw FileNotFoundException(dot.location.file)
    }

}
