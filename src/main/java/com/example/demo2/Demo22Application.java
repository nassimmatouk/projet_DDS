package com.example.demo2;

import org.hibernate.annotations.DialectOverride.OverridesAnnotation;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

@SpringBootApplication
public class Demo22Application implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Demo22Application.class, args);
	}


	@Override
	public void run(String... args) throws Exception{

		Path folderPath = Paths.get("C:/Users/userlocal/Desktop/M1/DS/jsonFiles");
		
        //watchFolder(folderPath);
	}

	private void watchFolder(Path folderPath){
		try{
			WatchService watchService = FileSystems.getDefault().newWatchService();
            folderPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

			while (true) {
				WatchKey key;
				try{
				key = watchService.take();

				
				} catch (InterruptedException ex) {
                    return;
                }

				for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();

                        
                        if (fileName.toString().endsWith(".json")) {
                            Path filePath = folderPath.resolve(fileName);
                            System.out.println("Fichier JSON détecté : " + filePath);
                            // Lire et afficher le contenu du fichier JSON
                            readAndPrintJsonFile(filePath);
                        }
                    }
                }
				boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
private void readAndPrintJsonFile(Path filePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            // Lit le fichier JSON et le convertit en Map
            Map<String, Object> jsonMap = objectMapper.readValue(new File(filePath.toString()), Map.class);
			for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
				System.out.println(entry.getValue());  
			}  
        }catch (IOException e) {
			System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage()); 
		}
    }
}

	

