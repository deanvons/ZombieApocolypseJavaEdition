package no.loopacademy.exceptions;

public class CarryWeightExceededException extends RuntimeException {
    
    public CarryWeightExceededException(String errorMessage){
        super(errorMessage);
    }

}
