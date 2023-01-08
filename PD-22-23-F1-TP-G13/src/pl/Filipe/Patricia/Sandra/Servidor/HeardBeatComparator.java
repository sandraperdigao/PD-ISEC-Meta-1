package pl.Filipe.Patricia.Sandra.Servidor;

import java.util.Comparator;

public class HeardBeatComparator implements Comparator<HeardBeat> {
    @Override
    public int compare(HeardBeat o1, HeardBeat o2) {
        if (o1.getNumLigacoesTCP() == o2.getNumLigacoesTCP())
            return 0;
        else if (o1.getNumLigacoesTCP() >  o2.getNumLigacoesTCP())
            return 1;
        else
            return -1;
    }
}
