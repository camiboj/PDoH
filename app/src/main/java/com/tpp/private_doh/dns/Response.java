package com.tpp.private_doh.dns;

import com.tpp.private_doh.util.Requester;

import java.util.List;

public class Response {
    private List<Question> questions;
    private List<Answer> answers;
    private Requester requester;
    private Runnable onWinning;

    public Response(List<Question> questions,
                    List<Answer> answers) {
        this.questions = questions;
        this.answers = answers;
        this.onWinning = () -> {
        };
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Response{");
        sb.append("questions=").append(questions);
        sb.append(", answers=").append(answers);
        return sb.toString();
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setOnWinning(Runnable f) {
        onWinning = f;
    }

    public void setAsWinner() {
        onWinning.run();
    }

    static public class Question {
        private String name;
        private int type;

        public Question(String name, int type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Question{");
            sb.append("name=").append(name);
            sb.append(", type=").append(type);
            return sb.toString();
        }
    }

    static public class Answer {
        private String name;
        private int type;
        private int ttl;
        private String data;

        public Answer(String name, int type, int ttl, String data) {
            this.name = name;
            this.type = type;
            this.ttl = ttl;
            this.data = data;
        }

        public String getName() {
            return name;
        }

        public int getType() {
            return type;
        }

        public int getTtl() {
            return ttl;
        }

        public String getData() {
            return data;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Answer{");
            sb.append("name=").append(name);
            sb.append(", type=").append(type);
            sb.append(", ttl=").append(ttl);
            sb.append(", data=").append(data);
            return sb.toString();
        }
    }
}
