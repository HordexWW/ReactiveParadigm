package com.griddynamics.sshmygin.reactive.onerrortest;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

public class OnErrorTest {

    @Test
    public void OnErrorReturnExample() {

        List<String> numbers = List.of("1", "2", "3", "4");
        Flux<String> numbersFlux = Flux.fromIterable(numbers)
                .map(this::throwExceptionIfNumberEqualsThree)
                .onErrorReturn("oops")
                .log();

        StepVerifier.create(numbersFlux)
                .expectNext("1", "2", "oops")
                .verifyComplete();
    }

    @Test
    public void OnErrorResumeExample() {

        List<String> numbers1to4 = List.of("1", "2", "3", "4");
        List<String> numbers5to7 = List.of("5", "6", "7");
        Flux<String> numbers5to7Flux = Flux.fromIterable(numbers5to7);

        Flux<String> numbers1to4Flux = Flux.fromIterable(numbers1to4)
                .map(this::throwExceptionIfNumberEqualsThree)
                .onErrorResume(ex -> numbers5to7Flux)
                .log();


        StepVerifier.create(numbers1to4Flux)
                .expectNext("1", "2", "5", "6", "7")
                .verifyComplete();
    }

    @Test
    public void OnErrorContinueExample() {

        List<String> numbers1to4 = List.of("1", "2", "3", "4");

        Flux<String> numbers1to4Flux = Flux.fromIterable(numbers1to4)
                .map(this::throwExceptionIfNumberEqualsThree)
                .onErrorContinue((ex, number) -> System.out.println("Caught " + ex))
                .log();

        StepVerifier.create(numbers1to4Flux)
                .expectNext("1", "2", "4")
                .verifyComplete();
    }

    @Test
    public void OnErrorMapExample() {

        List<String> numbers1to4 = List.of("1", "2", "3", "4");

        Flux<String> numbers1to4Flux = Flux.fromIterable(numbers1to4)
                .map(this::throwExceptionIfNumberEqualsThree)
                .onErrorMap(IllegalStateException.class,
                        ex -> {
                          throw new GettingNumberThreeException(ex.getMessage());
                        })
                .log();

        StepVerifier.create(numbers1to4Flux)
                .expectNext("1", "2")
                .expectError(GettingNumberThreeException.class)
                .verify();
    }

    private String throwExceptionIfNumberEqualsThree(String number) {
        if (number.equals("3")) {
            throw new GettingNumberThreeException("Got the number 3!!!!");
        } else {
            return number;
        }
    }
}
