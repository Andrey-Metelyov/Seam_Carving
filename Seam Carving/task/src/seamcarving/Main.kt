package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

val params = mutableMapOf<String, String>()

val debug = false

fun main(args: Array<String>) {
    readParams(args)
    val input = params["-in"]
    val output = params["-out"]

    makeEnergyImage(input, output)
}

private fun getLeft(x: Int, y: Int, image: BufferedImage): Color {
    val i = when (x) {
        0 -> 0
        image.width - 1 -> image.width - 3
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
        0 -> 2
        image.width - 1 -> image.width - 1
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
        image.height - 1 -> image.height - 3
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
        0 -> 2
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

    var maxEnergy = 0.0
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            val dx = getDx(getLeft(i, j, image), getRight(i, j, image))
            val dy = getDx(getTop(i, j, image), getBottom(i, j, image))
            energy[i][j] = kotlin.math.sqrt((dx + dy).toDouble())
            if (energy[i][j] > maxEnergy) {
                maxEnergy = energy[i][j]
            }
        }
    }

    for (i in 0 until image.width)
        for (j in 0 until image.height) {
            val norm = (255.0 * energy[i][j] / maxEnergy).toInt()
            image.setRGB(i, j, Color(norm, norm, norm).rgb)
        }

    ImageIO.write(image, "png", File(output))
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
