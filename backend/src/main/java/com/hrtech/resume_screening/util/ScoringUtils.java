package com.hrtech.resumescreening.util;

public class ScoringUtils {

    public static int calculateExperienceScore(int years){

        if(years >= 10){
            return 50;
        }
        else if(years >= 5){
            return 30;
        }
        else if(years >= 2){
            return 20;
        }
        else{
            return 10;
        }
    }

    public static int calculateSkillScore(int matchedSkills, int totalSkills){

        if(totalSkills == 0){
            return 0;
        }

        return (matchedSkills * 100) / totalSkills;
    }

}