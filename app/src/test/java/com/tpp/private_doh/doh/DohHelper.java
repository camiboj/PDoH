package com.tpp.private_doh.doh;

import static org.junit.Assert.assertEquals;

import java.util.List;

public class DohHelper {

    public String buildDohResponse() {
        return "{\"Status\": 0, \"TC\":false, \"RD\":true, \"RA\":true, \"AD\":false, \"CD\":false, \"Question\": [{ \"name\":\"apple.com.\", \"type\":1 }], \"Answer\": [ { \"name\":\"apple.com.\", \"type\":1, \"TTL\":3599,\"data\":\"17.178.96.59\" }, { \"name\":\"apple.com.\", \"type\":1, \"TTL\": 3599, \"data\":\"17.172.224.47\" },  { \"name\":\"apple.com.\", \"type\":1, \"TTL\":3599, \"data\":\"17.142.160.59\" } ], \"edns_client_subnet\":\"12.34.56.78/0\" }";
    }

    public void verifyDohResponse(DohResponse googleDohResponse) {
        List<DohResponse.Answer> answers = googleDohResponse.getAnswers();
        List<DohResponse.Question> questions = googleDohResponse.getQuestions();
        assertEquals(1, questions.size());
        assertEquals("apple.com.", questions.get(0).getName());
        assertEquals(1, questions.get(0).getType());
        assertEquals(3, answers.size());
        answers.forEach(answer -> {
                    assertEquals("apple.com.", answer.getName());
                    assertEquals(1, answer.getType());
                    assertEquals(3599, answer.getTtl());
                }
        );
        assertEquals("17.178.96.59", answers.get(0).getData());
        assertEquals("17.172.224.47", answers.get(1).getData());
        assertEquals("17.142.160.59", answers.get(2).getData());
    }
}
