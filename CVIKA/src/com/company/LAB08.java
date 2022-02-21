package com.company;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.SplittableRandom;

public class LAB08 {

    List<Person> personList;

    public void LoadCSV(String fileName){

        personList = new LinkedList<>();

        try(InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), "Cp1250");){
            Scanner sc = new Scanner(isr);
            while(sc.hasNext()){
                String[] parts = sc.nextLine().split(";");
                Person p = new Person(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]);
                personList.add(p);
            }
        }
        catch(FileNotFoundException e){
            System.err.println("File not found!");
        }
        catch(IOException e){
            System.err.println("Error reading file!");
        }
    }

    public void saveCSV(String filename){
        try(OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename), "Cp1250")){
            for(Person p : personList){
                String line = String.format("%s;%s;%s;%s;%s;%s;%s;%s;%s\n", p.name, p.position, p.positionDescription, p.doorNumber, p.phoneNumber, p.email, p.section, p.departmentAbb, p.departmentName);
                osw.write(line);
            }
        }
        catch (FileNotFoundException e){
            System.err.println("Error opening file");
        }
        catch (IOException e){
            System.err.println("Error writing file");
        }
    }

    public void saveObj(String filename){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))){
            oos.writeObject(personList);
        }
        catch (FileNotFoundException e){
            System.err.println("Error opening file");
        }
        catch (IOException e){
            System.err.println("Error reading file");
        }
    }

    public void loadObj(String filename){
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))){
            personList = (List<Person>) ois.readObject();
        }
        catch (FileNotFoundException e){
            System.err.println("Error opening file");
        }
        catch (IOException e){
            System.err.println("Error reading file");
        }catch (ClassNotFoundException e){
            System.err.println("Class not found!");
        }
    }
}
