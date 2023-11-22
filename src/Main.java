import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.TypeAlias;

public class Main {

    public interface Kernel32 {
        Kernel32 INSTANCE = LibraryLoader.create(Kernel32.class).load("kernel32");

        int GENERIC_READ = 0x80000000;
        int GENERIC_WRITE = 0x40000000;
        int FILE_SHARE_READ = 0x00000001;
        int FILE_SHARE_WRITE = 0x00000002;
        int CONSOLE_TEXTMODE_BUFFER = 1;

        Pointer CreateConsoleScreenBuffer(int dwDesiredAccess, int dwShareMode, Pointer lpSecurityAttributes, int dwFlags, Pointer lpScreenBufferData);
        void SetConsoleActiveScreenBuffer(Pointer hConsoleOutput);
        int WriteConsoleOutputCharacterW(Pointer hConsoleOutput, char[] lpCharacter, int nLength, int dwWriteCoord, Pointer lpNumberOfCharsWritten);
    }

    private static final int nScreenWidth = 240;
    private static final int nScreenHeight = 135;

    private static double lastPrint = 0;

    static Kernel32 kernel32 = Kernel32.INSTANCE;

    private static Pointer hConsole = kernel32.CreateConsoleScreenBuffer(
            Kernel32.GENERIC_WRITE | Kernel32.GENERIC_READ,
            Kernel32.FILE_SHARE_READ | Kernel32.FILE_SHARE_WRITE,
            null,
            Kernel32.CONSOLE_TEXTMODE_BUFFER,
            null
    );

    public static Pointer allocate(Runtime runtime, TypeAlias type) {
        return runtime.getMemoryManager().allocate(runtime.findType(type).size());
    }

    public static void writeCMD(char[] screen){
        try {
            if (screen.length == (nScreenWidth * nScreenHeight) + 1) {
                //long lpNumberOfCharsWritten = 0;
                Pointer lpNumberOfCharsWritten = Runtime.getSystemRuntime().getMemoryManager().allocate(Runtime.getSystemRuntime().findType(TypeAlias.int32_t).size());
                System.out.println(kernel32.WriteConsoleOutputCharacterW(hConsole, screen, (nScreenWidth * nScreenHeight) + 1, 0, lpNumberOfCharsWritten));
            }
        } catch (Exception t) {
            t.printStackTrace();
        }
    }

    public static void main(String[] args) {
        kernel32.SetConsoleActiveScreenBuffer(hConsole);

        char[] screen = new char[(nScreenWidth * nScreenHeight) + 1];
        for (int i = 0; i < screen.length - 1; i++) {
            screen[i] = '-';
        }
        screen[screen.length - 1] = '\0';

        lastPrint = System.nanoTime();

        int position = 0;
        while(true){
            while(System.nanoTime() - lastPrint < 4_166_666){}
            lastPrint = System.nanoTime();
            if(position > screen.length - 1) position = 0;
            screen[position] = '#';
            writeCMD(screen);
            screen[position] = '-';
            position++;
        }
    }
}


/*
    alternative code to work on later


    import jnr.ffi.LibraryLoader;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.Transient;
import jnr.ffi.types.size_t;

public class Main {

    public interface Kernel32 {
        Kernel32 INSTANCE = LibraryLoader.create(Kernel32.class).load("kernel32");

        int GENERIC_WRITE = 0x40000000;
        int FILE_SHARE_WRITE = 0x00000002;

        Pointer GetStdHandle(int nStdHandle);

        boolean SetConsoleCursorPosition(Pointer hConsoleOutput, int dwCursorPosition);

        boolean WriteFile(Pointer hFile, @In byte[] lpBuffer, int nNumberOfBytesToWrite, @Out @Transient int[] lpNumberOfBytesWritten, Pointer lpOverlapped);
        int GetLastError();
    }

    private static final int STD_OUTPUT_HANDLE = -11;
    private static final int BUFFER_SIZE = 240 * 135;
    private static final int SCREEN_WIDTH = 240;
    private static double lastPrint = 0;

    static Kernel32 kernel32 = Kernel32.INSTANCE;
    static Pointer hConsole = kernel32.GetStdHandle(STD_OUTPUT_HANDLE);
    public static void writeCMD(byte[] screen) {

        int[] lpNumberOfBytesWritten = new int[1];

        if (hConsole != null) {
            boolean success = kernel32.WriteFile(hConsole, screen, BUFFER_SIZE, lpNumberOfBytesWritten, null);
            success &= kernel32.SetConsoleCursorPosition(hConsole, 0);

            if (!success) {
                System.err.println("WriteFile failed with error: " + kernel32.INSTANCE.GetLastError());
            }
        } else {
            System.err.println("Failed to get console handle");
        }
    }

    public static void main(String[] args) {
        byte[] screen = new byte[BUFFER_SIZE];
        for (int i = 0; i < BUFFER_SIZE; i++) {
            screen[i] = '-';
        }

        int position = 0;
        while(true){
            while(System.nanoTime() - lastPrint < 16_666_666){}
            lastPrint = System.nanoTime();
            if(position > BUFFER_SIZE - 1) position = 0;
            screen[position] = '#';
            writeCMD(screen);
            screen[position] = '-';
            position++;
        }
    }
}

 */