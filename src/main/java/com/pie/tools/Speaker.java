package com.pie.tools;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.SpeakerModel;

import javax.sound.sampled.*;
import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.speech.synthesis.Voice;
import java.beans.PropertyVetoException;
import java.io.*;
import java.util.Date;
import java.util.Locale;

/**
 * @Author wangxiyue.xy@163.com
 * @Date 2022/6/6 18:34
 * @Description TODO :
 **/
public class Speaker {

    public static void main(String[] args) throws IOException {
        voiceToString();
//        audio();
    }

    public static void audio(){
        Recognizer recognizer = null;
        try {
            Model model = new Model("d:\\app\\vosk-model-small-cn-0.22");
            recognizer  = new Recognizer(model,22000.f);
            recognizer.setSpeakerModel(new SpeakerModel());
            recognizer.setPartialWords(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        AudioFormat audioFormat = new AudioFormat(22000,16,2,true,true);
        TargetDataLine line = null;
        SourceDataLine sourceDataLine = null;


        byte[] buff = new byte[1024];
         DataLine.Info info = new DataLine.Info(TargetDataLine.class,audioFormat);
        if(AudioSystem.isLineSupported(info)){
            try {
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class,audioFormat));
                sourceDataLine.open(audioFormat);
                sourceDataLine.start();
                FloatControl fc=(FloatControl)sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
                double value=2;
                float dB = (float) (Math.log(value==0.0?0.0001:value)/Math.log(10.0) * 20.0);
                fc.setValue(dB);
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(audioFormat);
                line.start();
                int off = 0;



                while (true){
                    int c= line.read(buff,0,buff.length);
//                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


                    String name= "d:\\wav\\"+ new Date().getTime() +".wav";
                    File outputStream = new File(name);
                    InputStream is = new ByteArrayInputStream(buff);
                    AudioSystem.write(new AudioInputStream(is,audioFormat,c),
                            AudioFileFormat.Type.WAVE,
                            outputStream);
                    byte[] bytes = new byte[1024];
                    int readSum = new FileInputStream(name).read(bytes,0,bytes.length);
                    off = off + readSum;
                    recognizer.acceptWaveForm(bytes,readSum);

                    String result= parseGetText(recognizer.getFinalResult());
                    if(result.length()>0){
                        System.out.println(result);
                    }
                    sourceDataLine.write(buff,0,c);

                }

            } catch (LineUnavailableException | IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void  freetts() throws IOException, InterruptedException, PropertyVetoException, EngineException, AudioException {
//        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        SynthesizerModeDesc desc = new SynthesizerModeDesc("synthesizer", "general",
                Locale.US, null, null);
        Synthesizer synthesizer = Central.createSynthesizer(desc);
        if(synthesizer==null){
            System.err.println("synthesizer = null");
        }

        synthesizer.allocate();
        synthesizer.resume();
        Voice voices[] = desc.getVoices();
        if(voices != null && voices.length > 0){
            synthesizer.getSynthesizerProperties().setVoice(voices[0]);
            synthesizer.speakPlainText("MPLS alarm: link down", null);
            synthesizer.waitEngineState(0x10000L);
        }


    }


    private static String parseGetText(String json){
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.get("text").toString().replaceAll(" ","");
    }

    public static void voiceToString(){
        //Model model = new Model("d:\\app\\vosk-model-small-cn-0.22");
        try {
            Model model = new Model("d:\\app\\vosk-model-small-cn-0.22");
//            Model model = new Model("D:\\app\\vosk-model-small-en-us-0.15");
            Recognizer recognizer = new Recognizer(model,22000.f);
            InputStream is = new FileInputStream("d:\\app\\a.wav");
//            InputStream is = new FileInputStream("d:\\app\\abc.wav");
            byte[] buff = new byte[10240];
            int c = is.read(buff,0,buff.length);
            while (c>0){
                recognizer.setSpeakerModel(new SpeakerModel());
                recognizer.setPartialWords(true);
                recognizer.acceptWaveForm(buff,c);
                c = is.read(buff,0,buff.length);
            }
            String result= parseGetText(recognizer.getFinalResult());
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
