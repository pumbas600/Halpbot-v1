package nz.pumbas.processes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.utilities.enums.StatusCode;

public class PipeProcess
{
    protected BufferedReader input;
    protected BufferedWriter output;
    protected Process process;

    public PipeProcess(String... commands)
    {
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectErrorStream(true);

        try {
            this.process = processBuilder.start();

            this.input = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
            this.output = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream()));

        } catch (IOException e) {
            ErrorManager.handle(e, "There was an error tying to start the process");
        }
    }

    public String pipe(String data)
    {
        if (!"quit".equalsIgnoreCase(data)) {

            try {
                this.output.write(data + "\n");
                this.output.flush();
                return this.input.readLine();
            } catch (IOException e) {
                ErrorManager.handle(e, "There was an error writing to the process.");
            }
        }

        return "";
    }

    public StatusCode pipeRequest(String data)
    {
        String result = this.pipe(data);
        return result.isEmpty() ? StatusCode.UNKNOWN : StatusCode.of(result);
    }

    public String getResponse()
    {
        try {
            return this.input.readLine();
        } catch (IOException e) {
            ErrorManager.handle(e);
        }
        return "";
    }

    public void printInput() {
        try {
            String line;
            while (null != (line = this.input.readLine())) {
                System.out.println(line);
            }
        } catch (IOException e) {
            ErrorManager.handle(e);
        }
    }

    public void closeProcess() {
        System.out.println("Closing process");
        this.transmitEndProcess();

        try {
            this.input.close();
            this.output.close();
            this.process.destroy();

        } catch (IOException e) {
            ErrorManager.handle(e);
        }
    }

    public void transmitEndProcess() {
        try {
            this.output.write("quit");
            this.output.flush();
        } catch (IOException e) {
            ErrorManager.handle(e, "Error ending process");
        }
    }
}
