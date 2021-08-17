package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Integer.max
import java.lang.Integer.min
import javax.imageio.ImageIO

val params = mutableMapOf<String, String>()

fun main(args: Array<String>) {
    readParams(args)
    val input = params["-in"]
    val output = params["-out"]

    makeEnergyImage(input, output)
}

private fun makeEnergyImage(input: String?, output: String?) {
    val image = ImageIO.read(File(input))
    val energy = Array(image.width) { Array(image.height) { 0.0 } }
    System.err.println("image ${image.width} x ${image.height}")
    var maxEnergy = 0.0
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            val left = Color(image.getRGB(max(i - 1, 0), j))
            val right = Color(image.getRGB(min(i + 1, image.width - 1), j))
            val top = Color(image.getRGB(i, max(j - 1, 0)))
            val bottom = Color(image.getRGB(i, min(j + 1, image.height - 1)))

            val dx = (left.red - right.red) * (left.red - right.red)
            +(left.green - right.green) * (left.green - right.green)
            +(left.blue - right.blue) * (left.blue - right.blue)
            val dy = (bottom.red - top.red) * (bottom.red - top.red)
            +(bottom.green - top.green) * (bottom.green - top.green)
            +(bottom.blue - top.blue) * (bottom.blue - top.blue)
            energy[i][j] = kotlin.math.sqrt((dx + dy).toDouble())
            System.err.println(
                "E($i,$j)=${energy[i][j]} " +
                        "left: ${max(i - 1, 0)},$j" +
                        " right: ${min(i + 1, image.width - 1)},$j" +
                        " top: $i,${max(j - 1, 0)}" +
                        " bottom: $i,${min(j + 1, image.height - 1)}"
            )
            if (energy[i][j] > maxEnergy) {
                maxEnergy = energy[i][j]
            }
        }
    }
    for (i in 0 until image.width)
        for (j in 0 until image.height)
            image.setRGB(i, j, (255.0 * energy[i][j] / maxEnergy).toInt())

    ImageIO.write(image, "png", File(output))
}


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
