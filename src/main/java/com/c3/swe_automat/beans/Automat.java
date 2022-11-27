package com.c3.swe_automat.beans;

import com.c3.swe_automat.entitys.database.*;
import com.c3.swe_automat.enums.SceneEnum;
import com.c3.swe_automat.repos.AdminAccountRepo;
import com.c3.swe_automat.repos.HaltestelleRepo;
import com.c3.swe_automat.repos.SettingsRepo;
import com.c3.swe_automat.service.AdminSettings;
import com.c3.swe_automat.service.SceneService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class Automat {

    private final SceneService sceneService;
    private final HaltestelleRepo haltestelleRepo;
    private final AdminAccountRepo adminAccountRepo;
    private final SettingsRepo settingsRepo;

    @Getter
    @Setter
    private boolean locked = false;


    @Getter
    private ShoppingCart shoppingCart;

    @Getter
    private final Bank bank;

    public void lockAutomat() {
        locked = true;
        sceneService.startScene(SceneEnum.U00START_WINDOW);
    }

    public void unlockAutomat() {
        locked = false;
    }

    @PostConstruct
    @Transactional
    @SneakyThrows
    public void createAllStuff() {
        //create all stops
        List<Haltestelle> haltestellen = new ArrayList<>();
        haltestellen.add(new Haltestelle("Untersee", 3));
        haltestellen.add(new Haltestelle("Bruchdorf", 3));
        haltestellen.add(new Haltestelle("Stolze", 3));
        haltestellen.add(new Haltestelle("Wolfhain", 3));
        haltestellen.add(new Haltestelle("Rissstein", 3));
        haltestellen.add(new Haltestelle("Altschauerberg", 3));
        haltestellen.add(new Haltestelle("Simmerstein", 3));
        haltestellen.add(new Haltestelle("Bad Leer", 3));
        haltestellen.add(new Haltestelle("Nebelburg", 3));
        haltestellen.add(new Haltestelle("Obertal", 3));
        haltestellen.add(new Haltestelle("Neudorf", 3));
        haltestellen.add(new Haltestelle("Düstersteig", 3));
        haltestellen.add(new Haltestelle("Tanntal", 3));
        haltestellen.add(new Haltestelle("Sems", 3));
        haltestellen.add(new Haltestelle("Rotburg", 3));
        haltestellen.add(new Haltestelle("Olde", 3));
        haltestellen.add(new Haltestelle("Echsenstadt", 2));
        haltestellen.add(new Haltestelle("Rußstein", 2));
        haltestellen.add(new Haltestelle("Nier1", 2));
        haltestellen.add(new Haltestelle("Keinfurt", 2));
        haltestellen.add(new Haltestelle("Trutztrum", 2));
        haltestellen.add(new Haltestelle("Hollstein", 2));
        haltestellen.add(new Haltestelle("Bierö", 2));
        haltestellen.add(new Haltestelle("Swefurt", 2));
        haltestellen.add(new Haltestelle("Ruste", 2));
        haltestellen.add(new Haltestelle("Wolkenstadt", 2));
        haltestellen.add(new Haltestelle("Wissstein", 2));
        haltestellen.add(new Haltestelle("Grasfeld", 1));
        haltestellen.add(new Haltestelle("Altstadt", 1));
        haltestellen.add(new Haltestelle("Nier2", 1));
        haltestellen.add(new Haltestelle("Rößle", 1));
        haltestellen.add(new Haltestelle("Lagos", 1));
        haltestellen.add(new Haltestelle("Rust", 1));

        for (Haltestelle h : haltestellen) {
            try {

                if (!haltestelleRepo.findAll(QHaltestelle.haltestelle.name.eq(h.getName())).iterator().hasNext()) {
                    log.info("Created Haltestelle " + haltestelleRepo.save(h).getName());
                }

            } catch (Exception e) {
            }

        }

        //create admin accounts
//        adminAccountRepo.deleteAll();
        List<AdminAccount> admins = new ArrayList<>();
        admins.add(new AdminAccount("admin1", "123"));

        for (AdminAccount a : admins) {
            try {
                if (!adminAccountRepo.findAll(QAdminAccount.adminAccount.username.eq(a.getUsername())).iterator().hasNext()) {
                    log.info("Created Account " + adminAccountRepo.save(a).getUsername());
                }
            } catch (Exception ignored) {
            }

        }
        List<AdminAccount> accs = adminAccountRepo.findAll();
        for (AdminAccount a : accs) {
            System.out.println(a.getUsername());
        }

        List<Settings> settingsList = new ArrayList<>();
        settingsList.add(new Settings("timeout", "180000"));
        settingsList.add(new Settings("endscreen", "7000"));
        settingsList.add(new Settings("accessCombination", "lrl"));
        settingsList.add(new Settings("adultA", "200"));
        settingsList.add(new Settings("adultB", "250"));
        settingsList.add(new Settings("adultC", "300"));
        settingsList.add(new Settings("childA", "130"));
        settingsList.add(new Settings("childB", "160"));
        settingsList.add(new Settings("childC", "190"));
        settingsList.add(new Settings("seniorA", "180"));
        settingsList.add(new Settings("seniorB", "220"));
        settingsList.add(new Settings("seniorC", "260"));
        settingsList.add(new Settings("discountA", "150"));
        settingsList.add(new Settings("discountB", "190"));
        settingsList.add(new Settings("discountC", "230"));
        settingsList.add(new Settings("locked", "false"));

        for (Settings s : settingsList) {
            try {
                if ((!settingsRepo.findAll(QSettings.settings.property.eq(s.getProperty())).iterator().hasNext())) {
                    log.info("Created Setting " + settingsRepo.save(s).getProperty());
                }
            } catch (Exception ignored) {
            }
        }

        fillAdminSettings();
    }

    public void updateDbAdminPassword(String accountname, String newPassword) {
        AdminAccount adminAccount = adminAccountRepo.findOne(QAdminAccount.adminAccount.username.eq(accountname)).get();

        if(!adminAccount.getPassword().equals(newPassword)) {
            adminAccountRepo.delete(adminAccount);
            adminAccount.setPassword(newPassword);
            adminAccountRepo.save(adminAccount);
            log.info("AdminAccounts: Changed password from user '" + accountname + "'");
        }
    }

    public void updateDbWithAdminSettings() {
        List<Settings> dbSettings = new ArrayList<>(settingsRepo.findAll());

        for (Settings s : dbSettings) {
            boolean updated = false;
            String val = "";
            String type = "";
            switch (s.getProperty()) {
                case "timeout":
                    val = String.valueOf(AdminSettings.getTimeout());
                    type = "Timeout";
                    break;
                case "endscreen":
                    val = String.valueOf(AdminSettings.getEndscreen());
                    type = "Endscreen";
                    break;
                case "accessCombination":
                    val = AdminSettings.getAccessCombination();
                    type = "Access-Combination";
                    break;

                case "adultA":
                    val = String.valueOf(AdminSettings.getAdultA());
                    type = "Adult A";
                    break;
                case "adultB":
                    val = String.valueOf(AdminSettings.getAdultB());
                    type = "Adult B";
                    break;
                case "adultC":
                    val = String.valueOf(AdminSettings.getAdultC());
                    type = "Adult C";
                    break;

                case "childA":
                    val = String.valueOf(AdminSettings.getChildA());
                    type = "Child A";
                    break;
                case "childB":
                    val = String.valueOf(AdminSettings.getChildB());
                    type = "Child B";
                    break;
                case "childC":
                    val = String.valueOf(AdminSettings.getChildC());
                    type = "Child C";
                    break;

                case "seniorA":
                    val = String.valueOf(AdminSettings.getSeniorA());
                    type = "Senior A";
                    break;
                case "seniorB":
                    val = String.valueOf(AdminSettings.getSeniorB());
                    type = "Senior B";
                    break;
                case "seniorC":
                    val = String.valueOf(AdminSettings.getSeniorC());
                    type = "Senior C";
                    break;

                case "discountA":
                    val = String.valueOf(AdminSettings.getDiscountA());
                    type = "Discount A";
                    break;
                case "discountB":
                    val = String.valueOf(AdminSettings.getDiscountB());
                    type = "Discount B";
                    break;
                case "discountC":
                    val = String.valueOf(AdminSettings.getDiscountC());
                    type = "Discount C";
                    break;

                case "locked":
                    val = String.valueOf(this.isLocked());
                    type = "Lock-Status";
                    break;
            }

            if (!s.getValue().equals(val)) {
                settingsRepo.delete(s);
                s.setValue(val);
                settingsRepo.save(s);
                log.info("Settings: Updated " + type + " in DB");
            }
        }
    }

    private void fillAdminSettings() {
        List<Settings> dbSettings = new ArrayList<>(settingsRepo.findAll());
        for (Settings s : dbSettings) {
            System.out.println(s.getProperty() + " | " + s.getValue());
            switch (s.getProperty()) {
                case "timeout":
                    AdminSettings.setTimeout(Integer.parseInt(s.getValue()));
                    break;
                case "endscreen":
                    AdminSettings.setEndscreen(Integer.parseInt(s.getValue()));
                    break;
                case "accessCombination":
                    AdminSettings.setAccessCombination(s.getValue());
                    break;

                case "adultA":
                    AdminSettings.setAdultA(Integer.parseInt(s.getValue()));
                    break;
                case "adultB":
                    AdminSettings.setAdultB(Integer.parseInt(s.getValue()));
                    break;
                case "adultC":
                    AdminSettings.setAdultC(Integer.parseInt(s.getValue()));
                    break;

                case "childA":
                    AdminSettings.setChildA(Integer.parseInt(s.getValue()));
                    break;
                case "childB":
                    AdminSettings.setChildB(Integer.parseInt(s.getValue()));
                    break;
                case "childC":
                    AdminSettings.setChildC(Integer.parseInt(s.getValue()));
                    break;

                case "seniorA":
                    AdminSettings.setSeniorA(Integer.parseInt(s.getValue()));
                    break;
                case "seniorB":
                    AdminSettings.setSeniorB(Integer.parseInt(s.getValue()));
                    break;
                case "seniorC":
                    AdminSettings.setSeniorC(Integer.parseInt(s.getValue()));
                    break;

                case "discountA":
                    AdminSettings.setDiscountA(Integer.parseInt(s.getValue()));
                    break;
                case "discountB":
                    AdminSettings.setDiscountB(Integer.parseInt(s.getValue()));
                    break;
                case "discountC":
                    AdminSettings.setDiscountC(Integer.parseInt(s.getValue()));
                    break;

                case "locked":
                    boolean lockStatus = Boolean.parseBoolean(s.getValue());
                    AdminSettings.setLocked(lockStatus);
                    this.locked = lockStatus;
                    break;
            }
        }
    }
}