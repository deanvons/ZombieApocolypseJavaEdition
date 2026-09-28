package no.loopacademy.exceptions;

public class CarryWeightExceededException extends BusinessRuleException {
    
    public CarryWeightExceededException(String errorMessage){
        super(errorMessage);
    }

}
