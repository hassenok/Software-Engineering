package com.c3.swe_automat.entitys.database;

import com.c3.swe_automat.enums.Coin;
import com.c3.swe_automat.exeptions.CoinException;
import com.c3.swe_automat.exeptions.PapierExeption;
import com.c3.swe_automat.repos.CoinStorageRepo;
import com.c3.swe_automat.repos.PaperStorageRepo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class Bank {

    private final CoinStorageRepo coinStorageRepo;
    private final PaperStorageRepo paperStorageRepo;

    private List<CoinStorage> coinStorages;

    private PaperStorage paperStorage;

    public CoinStorage getCoinStorage(Coin coin) throws CoinException {
        List<CoinStorage> collect = coinStorages.stream().filter(x -> x.getCoin().value == coin.value).collect(Collectors.toList());
        if (collect.isEmpty()) throw new CoinException("Coin type does not exist in Bank");
        return collect.get(0);
    }

    public CoinStorage getCoinStorage(int value) throws CoinException {
        List<CoinStorage> collect = coinStorages.stream().filter(x -> x.getCoin().value == value).collect(Collectors.toList());
        if (collect.isEmpty()) throw new CoinException("Coin type does not exist in Bank");
        return collect.get(0);
    }

    @SneakyThrows
    public int getAmount(Coin coin) {
        return getCoinStorage(coin).amount;
    }

    @SneakyThrows
    public int getAmount(int value) {
        return getCoinStorage(value).amount;
    }


    private boolean wouldTransactionWork(CoinStorage coinStorage, int amount) {
        return coinStorage.getAmount() + amount <= coinStorage.getMaxAmount() && coinStorage.getAmount() + amount >= 0;

    }

    private void save(CoinStorage c) {
        coinStorageRepo.save(c);
    }

    public void addPaper(int amount) throws PapierExeption {
        if (paperStorage.getAmount() + amount > paperStorage.getMaxAmount() || paperStorage.getAmount() + amount < 0)
            throw new PapierExeption("nicht genug Papier vorhanden");
        paperStorage.setAmount(paperStorage.getAmount() + amount);
        paperStorageRepo.save(paperStorage);
    }

    public void setPaperMax() {
        paperStorage.setAmount(paperStorage.getMaxAmount());
        paperStorageRepo.save(paperStorage);
    }

    public void setPaperMin() {
        paperStorage.setAmount(0);
        paperStorageRepo.save(paperStorage);
    }


    public void add(Coin coin, int amount) throws CoinException {
        CoinStorage coinStorage = getCoinStorage(coin);
        if (!wouldTransactionWork(coinStorage, amount)) throw new CoinException("CoinStorage is full");
        coinStorage.setAmount(coinStorage.getAmount() + amount);
        save(coinStorage);
    }

    public void fillMax(Coin coin) throws CoinException {
        CoinStorage c = getCoinStorage(coin);
        c.setAmount(c.maxAmount);
        save(c);
    }

    public void fillMin(Coin coin) throws CoinException {
        CoinStorage c = getCoinStorage(coin);
        c.setAmount(0);
        save(c);
    }

    public void add(int value, int amount) throws CoinException {
        CoinStorage coinStorage = getCoinStorage(value);
        if (!wouldTransactionWork(coinStorage, amount)) throw new CoinException("CoinStorage is full");
        coinStorage.setAmount(coinStorage.getAmount() + amount);
        save(coinStorage);
    }

    public void sub(int value, int amount) throws CoinException {
        add(value, amount * -1);
    }

    public void sub(Coin coin, int amount) throws CoinException {
        add(coin, amount * -1);
    }

    public List<Coin> generateChange(int amount) throws CoinException {
        List<CoinStorage> sorted = this.coinStorages.stream().filter(x -> x.amount > 0).sorted((y, x) -> x.getCoin().value - y.getCoin().value).collect(Collectors.toList());
        List<Coin> out = new ArrayList<>();
        for (CoinStorage c : sorted) {
            while (c.amount > 0) {
                if (amount - c.getCoin().value < 0 || amount <= 0) break;
                try {
                    sub(c.getCoin(), 1);
                } catch (CoinException e) {
                    break;
                }
                out.add(c.getCoin());
                amount -= c.getCoin().value;
            }
        }
        if (amount != 0) {
            log.warn("Problem mit Rückgeld {}ct fehlen", amount);
            throw new CoinException("Rückgeld kann nicht generiert werden");
        }

        return out;
    }

    @PostConstruct
    private void postConstruct() {
        coinStorages = new ArrayList<>();
        List<CoinStorage> coins = new ArrayList<>();

        coins.add(new CoinStorage(Coin.EIN_CENT, 0, 150, 0));
        coins.add(new CoinStorage(Coin.ZWEI_CENT, 0, 150, 0));
        coins.add(new CoinStorage(Coin.FUENF_CENT, 0, 150, 0));
        coins.add(new CoinStorage(Coin.ZEHN_CENT, 0, 150, 5));
        coins.add(new CoinStorage(Coin.ZWANZIG_CENT, 0, 150, 5));
        coins.add(new CoinStorage(Coin.FUENFZIG_CENT, 0, 150, 5));
        coins.add(new CoinStorage(Coin.EIN_EURO, 0, 150, 5));
        coins.add(new CoinStorage(Coin.ZWEI_EURO, 0, 150, 5));
        coins.add(new CoinStorage(Coin.FUENF_EURO, 0, 200, 2));
        coins.add(new CoinStorage(Coin.ZEHN_EURO, 0, 200, 5));
        coins.add(new CoinStorage(Coin.ZWANZIG_EURO, 0, 200, 0));
        coins.add(new CoinStorage(Coin.FUENFZIG_EURO, 0, 200, 0));

        paperStorage = new PaperStorage(1000, 0, 10);
        Optional<PaperStorage> one = paperStorageRepo.findOne(QPaperStorage.paperStorage.maxAmount.eq(paperStorage.getMaxAmount()).and(QPaperStorage.paperStorage.pufferAmount.eq(paperStorage.getPufferAmount())));
        if (one.isEmpty()) {
            log.info("Created PaperStorage " + paperStorageRepo.save(paperStorage));
        } else {
            paperStorage = one.get();
        }

        for (CoinStorage c : coins) {
            try {
                Optional<CoinStorage> two = coinStorageRepo.findOne(QCoinStorage.coinStorage.coin.eq(c.getCoin()).and(QCoinStorage.coinStorage.pufferAmount.eq(c.getPufferAmount())));
                if (two.isEmpty()) {
                    log.info("Created Coin " + coinStorageRepo.save(c).getCoin());
                    coinStorages.add(c);
                } else {
                    coinStorages.add(two.get());
                }

            } catch (Exception e) {
            }
        }
    }

    public boolean isInPaperPuffer() {
        return paperStorage.getAmount() < paperStorage.getPufferAmount();
    }

    public boolean isInCoinPuffer() {
        for (CoinStorage c : coinStorages) {
            if (c.amount < c.getPufferAmount() || c.maxAmount - c.amount < c.getPufferAmount()) {
                return true;
            }
        }
        return false;
    }


    public int getPapier() {
        return paperStorage.getAmount();
    }
}
