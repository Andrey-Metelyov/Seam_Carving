package seamcarving

import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt
import kotlin.math.min
import kotlin.math.max

val params = mutableMapOf<String, String>()

val debug = false

fun main(args: Array<String>) {
    System.err.println(args.joinToString())
    readParams(args)
    val input = params["-in"]
    val output = params["-out"]

    val image = ImageIO.read(File(input))

    val width = params["-width"]?.toInt() ?: 0
    val height = params["-height"]?.toInt() ?: 0

    var result: BufferedImage? = null

    repeat(width) {
        result = deleteSeam(result ?: image)
    }

    result = result!!.transpose()

    repeat(height) {
        result = deleteSeam(result!!)
    }

    ImageIO.write(result!!.transpose(), "png", File(output))
}


private fun BufferedImage.transpose(): BufferedImage {
    val result = BufferedImage(this.height, this.width, this.type)
    for (x in 0 until result.width)
        for (y in 0 until result.height)
            result.setRGB(x, y, this.getRGB(y, x))
    return result
}

fun deleteSeam(image: BufferedImage): BufferedImage {
    val energy = Array(image.width) { Array(image.height) { 0.0 } }
//    System.err.println("image ${image.width} x ${image.height}")
//    System.err.println("energy ${energy.size} x ${energy[0].size}")

    val maxEnergy = getEnergy(image, energy)
    val seam = energy.copyOf()
    for (j in 1 until seam[0].size) {
        for (i in seam.indices) {
            val min = min3(topLeft(i, j, seam), top(i, j, seam), topRight(i, j, seam))
            seam[i][j] += min
        }
    }

    val path = Array(image.height) { 0 }

    var min = 0
    for (col in 1..seam.lastIndex) {
        if (seam[col][seam[0].lastIndex] < seam[min][seam[0].lastIndex]) {
            min = col
        }
    }
    System.err.println("first min=$min")
    path[path.lastIndex] = min
    var prevMin = min
    for (row in seam[0].lastIndex - 1 downTo 0) {
        min = max(prevMin - 1, 0)
        for (col in prevMin..min(prevMin + 1, seam.lastIndex)) {
//            System.err.println("col=$col min=$min row=$row: ${seam[col][row]} ? ${seam[min][row]}")
            if (seam[col][row] < seam[min][row]) {
                min = col
            }
        }
        prevMin = min
        path[row] = min
    }
//    System.err.println("path:")
//    System.err.println(path.joinToString(" "))
    val result = BufferedImage(image.width - 1, image.height, image.type)
//    System.err.println("result image ${result.width} x ${result.height}")
    for (y in 0 until result.height) {
        for (x in 0 until path[y]) {
            result.setRGB(x, y, image.getRGB(x, y))
        }
        for (x in path[y] until result.width) {
            result.setRGB(x, y, image.getRGB(x + 1, y))
        }
    }
//    System.err.println("result image ${result.width} x ${result.height}")
    return result
}

fun highlightSeam(image: BufferedImage): BufferedImage {
    val energy = Array(image.width) { Array(image.height) { 0.0 } }
    System.err.println("image ${image.width} x ${image.height}")
    System.err.println("energy ${energy.size} x ${energy[0].size}")

    val maxEnergy = getEnergy(image, energy)
    val seam = energy.copyOf()
    for (j in 1 until seam[0].size) {
        for (i in seam.indices) {
            val min = min3(topLeft(i, j, seam), top(i, j, seam), topRight(i, j, seam))
            seam[i][j] += min
        }
    }

    val path = Array(image.height) { 0 }

    var min = 0
    for (col in 1..seam.lastIndex) {
        if (seam[col][seam[0].lastIndex] < seam[min][seam[0].lastIndex]) {
            min = col
        }
    }
//    System.err.println("first min=$min")
    path[path.lastIndex] = min
    var prevMin = min
    for (row in seam[0].lastIndex - 1 downTo 0) {
        min = max(prevMin - 1, 0)
        for (col in prevMin..min(prevMin + 1, seam.lastIndex)) {
            System.err.println("col=$col min=$min row=$row: ${seam[col][row]} ? ${seam[min][row]}")
            if (seam[col][row] < seam[min][row]) {
                min = col
            }
        }
        prevMin = min
        path[row] = min
    }
    System.err.println("path:")
    System.err.println(path.joinToString(" "))
    for (row in 0 until image.height) {
        image.setRGB(path[row], row, Color.RED.rgb)
    }
    return image
}

fun topRight(col: Int, row: Int, seam: Array<Array<Double>>): Double {
    return when (col) {
        seam.size - 1 -> seam[seam.size - 1][row - 1]
        else -> seam[col + 1][row - 1]
    }
}

fun top(col: Int, row: Int, seam: Array<Array<Double>>): Double = seam[col][row - 1]

fun topLeft(col: Int, row: Int, seam: Array<Array<Double>>): Double {
    return when (col) {
        0 -> seam[0][row - 1]
        else -> seam[col - 1][row - 1]
    }
}

fun min3(a: Double, b: Double, c: Double): Double = kotlin.math.min(a, kotlin.math.min(b, c))

private fun getLeft(x: Int, y: Int, image: BufferedImage): Color {
    val i = when (x) {
        0 -> 0
        image.width - 1 -> (image.width - 3).coerceAtLeast(0)
        else -> x - 1
    }
    if (debug) System.err.println("($x,$y) -> left  =($i,$y)")
    return Color(image.getRGB(i, y))
//    return when(x) {
//        0 -> Color(image.getRGB(0, y))
//        image.width - 1 -> Color(image.getRGB(image.width - 3, y))
//        else -> Color(image.getRGB(x - 1, y))
//    }
}

private fun getRight(x: Int, y: Int, image: BufferedImage): Color {
    val i = when (x) {
        0 -> 2.coerceAtMost(image.width - 1)
        image.width - 1 -> (image.width - 1)
        else -> x + 1
    }
    if (debug) System.err.println("($x,$y) -> right =($i,$y)")
    return Color(image.getRGB(i, y))
//    return when(x) {
//        0 -> Color(image.getRGB(2, y))
//        image.width - 1 -> Color(image.getRGB(image.width - 1, y))
//        else -> Color(image.getRGB(x + 1, y))
//    }
}

private fun getTop(x: Int, y: Int, image: BufferedImage): Color {
    val j = when (y) {
        0 -> 0
        image.height - 1 -> (image.height - 3).coerceAtLeast(0)
        else -> y - 1
    }
    if (debug) System.err.println("($x,$y) -> top   =($x,$j)")
    return Color(image.getRGB(x, j))
//    return when(y) {
//        0 -> Color(image.getRGB(x, 0))
//        image.height - 1 -> Color(image.getRGB(x, y - 3))
//        else -> Color(image.getRGB(x, y  - 1))
//    }
}

private fun getBottom(x: Int, y: Int, image: BufferedImage): Color {
    val j = when (y) {
        0 -> 2.coerceAtMost(image.height - 1)
        image.height - 1 -> image.height - 1
        else -> y + 1
    }
    if (debug) System.err.println("($x,$y) -> bottom=($x,$j)")
    return Color(image.getRGB(x, j))
//    return when(y) {
//        0 -> Color(image.getRGB(x, 2))
//        image.height - 1 -> Color(image.getRGB(x, image.height - 1))
//        else -> Color(image.getRGB(x, y  + 1))
//    }
}

private fun makeEnergyImage(input: String?, output: String?) {
    val image = ImageIO.read(File(input))
    val energy = Array(image.width) { Array(image.height) { 0.0 } }
    System.err.println("image ${image.width} x ${image.height}")

    val maxEnergy = getEnergy(image, energy)

    for (i in 0 until image.width)
        for (j in 0 until image.height) {
            val norm = (255.0 * energy[i][j] / maxEnergy).toInt()
            image.setRGB(i, j, Color(norm, norm, norm).rgb)
        }

    ImageIO.write(image, "png", File(output))
}

private fun getEnergy(
    image: BufferedImage,
    energy: Array<Array<Double>>
): Double {
    var maxEnergy = 0.0
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            val dx = getDx(getLeft(i, j, image), getRight(i, j, image))
            val dy = getDx(getTop(i, j, image), getBottom(i, j, image))
            energy[i][j] = sqrt((dx + dy).toDouble())
            if (energy[i][j] > maxEnergy) {
                maxEnergy = energy[i][j]
            }
        }
    }
    return maxEnergy
}

private fun getDx(a: Color, b: Color): Int =
    (a.red - b.red) * (a.red - b.red) + (a.green - b.green) * (a.green - b.green) + (a.blue - b.blue) * (a.blue - b.blue)

private fun makeNegativeImage(input: String?, output: String?) {
    val image = ImageIO.read(File(input))
    for (i in 0 until image.width)
        for (j in 0 until image.height)
            image.setRGB(i, j, negative(image.getRGB(i, j)))

    ImageIO.write(image, "png", File(output))
}

fun negative(rgb: Int): Int {
    val color = Color(rgb)
    return Color(255 - color.red, 255 - color.green, 255 - color.blue).rgb
}

fun readParams(args: Array<String>) {
    for (i in 0..args.lastIndex) {
        if (args[i].startsWith("-")) {
            params[args[i]] = args[i + 1]
            System.err.println("${args[i]} = ${args[i + 1]}")
        } else {
            continue
        }
    }
}

private fun makeXImage() {
    println("Enter rectangle width:")
    val width = readLine()!!.toInt()
    println("Enter rectangle height:")
    val height = readLine()!!.toInt()
    println("Enter output image name:")
    val name = readLine()!!
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g = image.graphics
    g.color = Color.RED
    g.drawLine(0, 0, width - 1, height - 1)
    g.drawLine(0, height - 1, width - 1, 0)
    ImageIO.write(image, "png", File(name))
}
