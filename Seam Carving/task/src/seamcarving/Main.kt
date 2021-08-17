package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

val params = mutableMapOf<String, String>()

fun main(args: Array<String>) {
    readParams(args)
    val input = params["-in"]
    val output = params["-out"]

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
