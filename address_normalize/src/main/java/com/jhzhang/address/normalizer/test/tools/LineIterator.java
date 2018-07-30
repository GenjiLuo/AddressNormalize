package com.jhzhang.address.normalizer.test.tools;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * 创建一个一个文件读行迭代器
 *
 * @author jhZhang
 * @date 2018/5/4
 */
public class LineIterator implements Iterator {
    private BufferedReader br = null;
    private String line = null;

    public LineIterator(File file) {
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public LineIterator(Path path) {
        this(path.toFile());
    }

    public LineIterator(String fileStr) {
        this(new File(fileStr));
    }

    @Override
    public boolean hasNext() {
        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line != null;
    }

    @Override
    public String next() {
        // todo 这里有问题，迭代器的next运行之后会找出下一个next，这里不行
        return line;
    }

    @Override
    public void remove() {
        new RuntimeException("unsupport opreator");
    }
}
