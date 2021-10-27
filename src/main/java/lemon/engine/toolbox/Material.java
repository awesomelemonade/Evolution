package lemon.engine.toolbox;

import lemon.engine.math.FloatData;

public class Material {
    String mtlName;
    float Ns;
    Color Ka;
    Color Kd;
    Color Ks;
    Color Ke;
    float Ni;
    float d;
    int illum;

    public Material() {
    }

    public void setMtlName(String mtlName) {
        this.mtlName = mtlName;
    }

    public void setNs(float ns) {
        Ns = ns;
    }

    public void setKa(Color ka) {
        Ka = ka;
    }

    public void setKd(Color kd) {
        Kd = kd;
    }

    public void setKs(Color ks) {
        Ks = ks;
    }

    public void setKe(Color ke) {
        Ke = ke;
    }

    public void setNi(float ni) {
        Ni = ni;
    }

    public void setD(float d) {
        this.d = d;
    }

    public void setIllum(int illum) {
        this.illum = illum;
    }

    @Override
    public String toString() {
        return mtlName + "\n" + Kd.toString() + "\n" + Ka.toString() + "\n" + Ks.toString();
    }
}