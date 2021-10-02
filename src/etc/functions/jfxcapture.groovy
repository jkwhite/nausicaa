import org.excelsi.nausicaa.NViewer
import org.excelsi.nausicaa.ca.*
import java.util.Random
import java.io.File

meta = { ca ->
    [
      'name' : 'Jfx Screen Capture',
      'args' : ['frames', 'fps', 'dir', 'prefix']
    ]
}


run = { ca, args, api ->
    def frames = args['frames'] as Integer
    def fps = args['fps'] as Double
    def dir = args['dir']
    def prefix = args['prefix']

    if(!new File(dir).exists()) {
        new File(dir).mkdirs()
    }
    def pd = NViewer.instance().planeDisplayProvider.activePlaneDisplay

    def cframe = 0
    def spf = 1d/fps
    api.progress.maximum = frames
    while(cframe<frames) {
        pd.save("${dir}/${prefix}-${cframe}.png", pd.rendering)
        Thread.sleep((spf*1000) as Integer)
        api.progress.current = ++cframe
    }
}
