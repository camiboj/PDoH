package com.mocyx.basic_client.doh;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

// TODO: delete lombok
import lombok.Value;

@Value
public class GoogleDohResponse {

    private boolean TC = false;
    private boolean RD = false;
    private boolean RA = false;
    private boolean AD = false;
    private boolean CD = false;
    @JsonProperty("Question")
    private List<Question> questions = new ArrayList<>();
    @JsonProperty("Answer")
    private List<Answer> answers = new ArrayList<>();
    public GoogleDohResponse() {
    } // needed by Jackson

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GoogleDohResponse {");
        sb.append("Question=").append(questions);
        sb.append(", Answers=").append(answers);
        sb.append('}');
        return sb.toString();
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    static public class Question {
        private String name;
        private int type;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Question{");
            sb.append("name=").append(name);
            sb.append(", type=").append(type);
            sb.append('}');
            return sb.toString();
        }

        public String getName() {
            return name;
        }

        public int getType() {
            return type;
        }
    }

    static public class Answer {
        private String name = "";
        private int type = 1;
        private int ttl = 1;
        private String data = "1.1.1.1";

        public Answer() {
        } // needed by Jackson


        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Answer{");
            sb.append("name=").append(name);
            sb.append(", type=").append(type);
            sb.append(", ttl=").append(ttl);
            sb.append(", data=").append(data);
            sb.append('}');
            return sb.toString();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
