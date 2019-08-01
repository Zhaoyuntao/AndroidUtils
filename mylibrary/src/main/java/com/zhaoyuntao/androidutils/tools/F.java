package com.zhaoyuntao.androidutils.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class F {

    private static Map<File, Task> map = new HashMap<>();

    /**
     * 同步拆分文件
     *
     * @param file
     * @param size
     * @return
     */
    public static byte[][] slipFile(File file, int size) {
        byte[][] packages = new byte[0][];
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            while (true) {
                byte[] data = new byte[size];
                int num = randomAccessFile.read(data);
                if (num == -1) {
                    break;
                }
                if (num < data.length) {
                    data = Arrays.copyOf(data, num);
                }
                packages = Arrays.copyOf(packages, packages.length + 1);
                packages[packages.length - 1] = data;
            }
//            S.s("文件大小:" + all);
//            S.s("拆分总数:" + packages.length + " 每份:" + size + " 总和:" + (packages.length * size - tail));
            randomAccessFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return packages;
    }

    /**
     * 异步拆分文件
     *
     * @param file
     * @param size
     * @param callBack
     */
    public static ZThread slipFile(final File file, final int size, final CallBack callBack) {
        ZThread zThread = new ZThread() {
            private FileInputStream fileInputStream;
            private RandomAccessFile randomAccessFile;
            private int count;
            private int index = 0;
            private int position = 0;
            private int filesize;
            @Override
            protected void init() {
                try {
                    fileInputStream = new FileInputStream(file);
                    filesize = fileInputStream.available();
//                    S.s("文件大小:" + filesize);
                    fileInputStream.close();
                    count = filesize / size;
                    if (filesize % size > 0) {
                        count += 1;//尾部的余数也算一个片段
                    }

                    randomAccessFile = new RandomAccessFile(file, "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void todo() {
                byte[] data = new byte[size];
                int num = 0;
                try {
                    num = randomAccessFile.read(data);
                    if (num == -1) {
                        close();
                        return;
                    }
                    if (num < data.length) {
                        data = Arrays.copyOf(data, num);
                    }
                    S.s("得到第[" + index + "/" + count + "]个片段,大小:" + num + "写入位置计算:" + position);
                    if (callBack != null) {
                        callBack.whenGotPiece(Arrays.copyOf(data, data.length), index++, count, position, filesize,this);
                    }
                    position += num;
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                }
            }

            @Override
            public void close() {
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                S.s("end , zthread.close");
                super.close();
            }
        };
        zThread.start();
        return zThread;
    }

    /**
     * 拆分byte arr,同步
     *
     * @param data
     * @param size
     * @return
     */
    public static byte[][] slipByteArrays(byte[] data, int size) {
        int length = data.length;
        int onePiece = size;
        int count = length / onePiece;
        if (length % onePiece > 0) {
            count++;
        }
        byte[][] packages = new byte[count][];
        for (int i = 0; i < packages.length; i++) {
            byte[] tmp = null;
            if (i == packages.length - 1) {
                tmp = Arrays.copyOfRange(data, i * onePiece, data.length);
            } else {
                tmp = Arrays.copyOfRange(data, i * onePiece, (i + 1) * onePiece);
            }
            packages[i] = tmp;
        }
        return packages;
    }

    /**
     * 组合byte arr,同步
     *
     * @param datas
     * @return
     */
    public static byte[] assembleByteArrays(byte[][] datas) {
        byte[] data = new byte[0];

        for (int i = 0; i < datas.length; i++) {
            byte[] tmp = datas[i];
            if (tmp != null) {
                data = Arrays.copyOf(data, data.length + tmp.length);
                System.arraycopy(tmp, 0, data, data.length - tmp.length, tmp.length);
            }
        }
        return data;
    }

    public static class TaskMsg {
        public long position;
        public int index;
        byte[] data;
    }

    public static class Task extends ZThread {
        RandomAccessFile randomAccessFile;
        BlockingDeque<TaskMsg> blockingDeque;
        File file;
        CallBack callBack;
        private int countNow;
        private int all;
        public Task(File file, CallBack callBack) {
            this.callBack = callBack;
            this.all = callBack.getCount();
            this.file = file;
            blockingDeque = new LinkedBlockingDeque<>();
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                randomAccessFile = new RandomAccessFile(file, "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                S.e(e);
            }
        }

        public void addPiece(byte[] data, long writePosition,int index) {
            TaskMsg taskMsg = new TaskMsg();
            taskMsg.data = data;
            taskMsg.position = writePosition;
            taskMsg.index=index;
            blockingDeque.add(taskMsg);
        }

        @Override
        protected void init() {
            if(callBack!=null){
                callBack.whenStartDownloading(file.getAbsolutePath());
            }
        }

        @Override
        protected void todo() {
            boolean completed = false;
            try {
                TaskMsg taskMsg = blockingDeque.take();
                countNow++;
                byte[] data = taskMsg.data;
                randomAccessFile.seek(taskMsg.position);
                randomAccessFile.write(data);
                S.s("正在接收文件[" + countNow + "/" + all + "]["+taskMsg.index+"] 数据长度:" + data.length + " 写入位置:" + taskMsg.position);
                if (callBack != null) {
                    callBack.whenDownloading(file.getAbsolutePath(),countNow / (float)all);
                }
                if (countNow >= all) {
//                    S.s("文件块全部接收完毕,总个数:" + all);
                    completed = true;
                }

            } catch (InterruptedException e) {
                completed = true;
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                completed = true;
                e.printStackTrace();
            } catch (IOException e) {
                completed = true;
                e.printStackTrace();
            } finally {
                if (completed) {
                    close();
                }
            }
        }


        @Override
        public void close() {
            map.remove(file);
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (callBack != null) {
                callBack.whenWriteOk(file.getAbsolutePath());
            }
            super.close();
        }

        public interface CallBack {
            void whenWriteOk(String filename);

            void whenDownloading(String filename,float percent);

            int getCount();

            void whenStartDownloading(String filename);
        }
    }


    /**
     * 组合文件,需要与closeFile一起使用
     *
     * @param file
     * @param data
     */
    public static void assembleFile(final int index,final File file, final byte[] data, final long position, Task.CallBack callBack) {
        if (file == null || data == null) {
            return;
        }
        Task task = map.get(file);
        if (task == null) {
            task = new Task(file, callBack);
            map.put(file, task);
            task.start();
        }
        task.addPiece(data, position,index);
    }

    /**
     * 组合文件,异步
     *
     * @param path
     * @param filename
     * @param datas
     * @param result
     */
    public static void assembleFile(final String path, final String filename, final byte[][] datas, final Result result) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String pathTmp = path;
                String filenameTmp = filename;
                byte[] fileBytes = assembleByteArrays(datas);
                if (pathTmp.startsWith("/")) {
                    pathTmp = pathTmp.substring(1);
                }
                if (pathTmp.endsWith("/")) {
                    pathTmp = pathTmp.substring(pathTmp.length() - 1);
                }
                if (filenameTmp.startsWith("/")) {
                    filenameTmp = filenameTmp.substring(1);
                }
                if (filenameTmp.endsWith("/")) {
                    filenameTmp = filenameTmp.substring(filenameTmp.length() - 1);
                }
                File file = new File(pathTmp, filenameTmp);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(fileBytes);
                    fileOutputStream.flush();
                    if (result != null) {
                        result.whenSuccess();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    if (result != null) {
                        result.whenFailed(e.getMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (result != null) {
                        result.whenFailed(e.getMessage());
                    }
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }


    public interface CallBack {
        void whenGotPiece(byte[] data, int index, int count, int position, long filesize,ZThread zThread);

    }

    public interface Result {
        void whenSuccess();

        void whenFailed(String msg);
    }
}
