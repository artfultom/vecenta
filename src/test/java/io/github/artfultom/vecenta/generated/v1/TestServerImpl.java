package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.generated.v1.math.*;

import java.util.List;
import java.util.Map;

public class TestServerImpl implements TestServer {

    @Override
    public Integer sum(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public String concat(String a, String b, String c) {
        return a + b + c;
    }

    @Override
    public Integer echo1(Integer a) {
        return a;
    }

    @Override
    public List<Integer> echo2(List<Integer> a) {
        return a;
    }

    @Override
    public Model3 echo3(Model3 a) {
        return a;
    }

    @Override
    public List<Model3> echo4(List<Model3> a) {
        return a;
    }

    @Override
    public Map<Integer, Model3> echo5(Map<Integer, Model3> a) {
        return a;
    }

    @Override
    public Map<Integer, List<Model3>> echo6(Map<Integer, List<Model3>> a) {
        return a;
    }

    @Override
    public List<List<String>> echo7(List<List<String>> a) {
        return a;
    }

    @Override
    public Map<Integer, List<List<Model3>>> echo8(Map<Integer, List<List<Model3>>> a) {
        return a;
    }

    @Override
    public Map<List<Model3>, List<List<Model3>>> echo9(Map<List<Model3>, List<List<Model3>>> a) {
        return a;
    }

    @Override
    public Integer supply() {
        return 42;
    }

    @Override
    public void consume(Integer a) {
    }

    @Override
    public void error1() {
        throw new RuntimeException();
    }

    @Override
    public void error2() throws FileNotFoundException, NewErrorException {
        throw new FileNotFoundException();
    }

    @Override
    public void error3() throws FileNotFoundException, IOException, EtcException {
        throw new EtcException();
    }


}
