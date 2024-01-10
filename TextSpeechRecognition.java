import java.util.Scanner;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.DataLine;
import javax.speech.Central;
import javax.speech.recognition.Recognizer;
import javax.speech.recognition.ResultAdapter;
import javax.speech.recognition.RuleGrammar;
import javax.sound.sampled.*;
import javax.speech.*;
import javax.speech.recognition.*;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

class TextSpeechRecognition {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose an option:");
        System.out.println("1. Text to Speech");
        System.out.println("2. Speech to Text");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        if (choice == 1) {
            // Text to Speech
            System.out.println("Enter the text you want to convert to speech:");
            String text = scanner.nextLine();
            textToSpeech(text);
        } else if (choice == 2) {
            System.out.println("Speak something for speech-to-text:");
            speechTotext();
        } else {
            System.out.println("Invalid choice.");
        }
    }


    public static void textToSpeech(String text) {
        Voice voice;
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us" + ".cmu_us_kal.KevinVoiceDirectory");
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice("kevin16");

        if (voice != null) {
            voice.allocate();
            voice.speak(text);
            voice.deallocate();
        } else {
            System.out.println("Cannot find the specified voice.");
        }
    }

    private static void speechTotext() {
        try {
            // Set up the recognizer
            Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
            Recognizer recognizer = Central.createRecognizer(null);
            recognizer.allocate();

            // Create a grammar-based rule
            RuleGrammar grammar = recognizer.getGrammar();
            grammar.loadJSGF("myGrammar", "grammar.jsgf"); // Replace "grammar.jsgf" with your grammar file

            // Start the recognizer
            recognizer.addResultListener(new ResultAdapter() {
                public void resultAccepted(ResultEvent e) {
                    Result result = (Result) (e.getSource());
                    ResultToken token = result.getBestToken();
                    if (token != null) {
                        String text = token.getSpokenText();
                        System.out.println("You said: " + text);
                    }
                }
            });

            recognizer.commitChanges();
            recognizer.requestFocus();
            recognizer.resume();

            // Capture audio
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[1024];
            int bytesRead;

            while (true) {
                bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    recognizer.processAudio(buffer, 0, bytesRead);;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}