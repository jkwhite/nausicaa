import org.excelsi.nausicaa.ca.*
import java.util.Random
import java.io.File

meta = { ca ->
    [
      'name' : 'Directory Visualizer',
      'args' : ['dir', 'imagedir', 'width', 'height', 'iter']
    ]
}

nvl = { x,y ->
    x!=null?x:y
}


run = { ca, args, api ->
    def dir = args['dir']
    def imgdir = args['imagedir']
    def w = nvl(args['width'],"-1") as Integer
    def h = nvl(args['height'],"-1") as Integer
    def iter = nvl(args['iter'], "10") as Integer
    def ccores = 4
    def prog = 0

    fs = new File(dir).listFiles().findAll { it.name.endsWith(".ca") }
    if(!new File(imgdir).exists()) {
        new File(imgdir).mkdirs()
    }

    api.progress.maximum = fs.size()
    for(f in fs) {
        try {
            def img = imgdir+"/"+f.getName()+".png"
            def imgcheck = img+"-${iter-1}.png"
            if(new File(imgcheck).exists()) {
                System.err.println("skipping ${f} -> ${imgcheck}")
                continue;
            }
            System.err.println("building ${f} -> ${imgcheck}")
            def cur = CA.fromFile(f.toString(), "text")
            if(w!=-1&&h!=-1) {
                cur.resize(w,h)
            }
            cur.compileRule()
                .stream(cur.createPlane(), api.pool, new GOptions(true, ccores, 1, 1.0).metaMode(cur.getMetaMode()))
                .limit(iter)
                .map(Pipeline.context("p", "i", Pipeline.identifier()))
                //.filter((c)->{c.get("i")==iter-1})
                .filter(Pipeline.filterIdentifier("i",iter-1))
                .map(Pipeline.toBufferedImage("p", "b"))
                .forEach(Pipeline
                    .write("b", "i", img)
                );
        }
        catch(Exception e) {
            System.err.println("failed on ${f}: ${e.toString()}")
            e.printStackTrace();
        }
        if(api.cancelled) break;
        api.progress.current = ++prog
    }
}
