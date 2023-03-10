package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.entity.Passive;
import com.nttdata.bootcamp.repository.PassiveRepository;
import com.nttdata.bootcamp.service.PassiveService;
import com.nttdata.bootcamp.service.SavingAccountService;
import com.nttdata.bootcamp.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Service implementation
@Service
public class SavingAccountServiceImpl implements SavingAccountService {

    @Autowired
    private PassiveRepository passiveRepository;
    @Autowired
    private PassiveService passiveService;

    @Override
    public Flux<Passive> findAllSavingAccount() {
        Flux<Passive> passives = passiveRepository
                .findAll()
                .filter(x -> x.getSaving());
        return passives;
    }

    @Override
    public Flux<Passive> findSavingAccountByCustomer(String dni) {
        Flux<Passive> passives = passiveRepository
                .findAll()
                .filter(x -> x.getSaving() && x.getDni().equals(dni));
        return passives;
    }

    @Override
    public Mono<Passive> findSavingAccountByAccountNumber(String accountNumber) {
        Mono<Passive> passiveMono = passiveRepository
                .findAll()
                .filter(x -> x.getSaving() && x.getAccountNumber().equals(accountNumber))
                .next();
        return passiveMono;
    }

    @Override
    public Mono<Passive> saveSavingAccount(Passive dataSavingAccount, Boolean creditCard) {
        Mono<Passive> passive = Mono.empty();
        dataSavingAccount.setFreeCommission(true);
        dataSavingAccount.setCommissionMaintenance(0);
        dataSavingAccount.setMovementsMonthly(true);
        dataSavingAccount.setSaving(true);
        dataSavingAccount.setCurrentAccount(false);
        dataSavingAccount.setFixedTerm(false);
        if(creditCard){
            dataSavingAccount.setDailyAverage(true);
            dataSavingAccount.setFlagVip(true);
            dataSavingAccount.setFlagPyme(false);
        }
        else{
            dataSavingAccount.setDailyAverage(false);
            dataSavingAccount.setFlagVip(false);
            dataSavingAccount.setFlagPyme(false);
        }


        if(dataSavingAccount.getTypeCustomer().equals(Constant.PERSONAL_CUSTOMER)){
            passive = passiveService.searchBySavingCustomer(dataSavingAccount);
        }
        if(dataSavingAccount.getTypeCustomer().equals(Constant.BUSINESS_CUSTOMER)){

        }

        return passive
                .flatMap(__ -> Mono.<Passive>error(new Error("The customer with DNI " + dataSavingAccount.getDni() + " have an account")))
                .switchIfEmpty(passiveRepository.save(dataSavingAccount));
        //return passiveSavingRepository.save(dataPassiveSaving);
    }

    @Override
    public Mono<Passive> updateSavingAccount(Passive dataSavingAccount) {
        Mono<Passive> passiveMono = findSavingAccountByAccountNumber(dataSavingAccount.getAccountNumber());
        try{
            Passive passive = passiveMono.block();
            passive.setLimitMovementsMonthly(dataSavingAccount.getLimitMovementsMonthly());
            passive.setModificationDate(dataSavingAccount.getModificationDate());
            return passiveRepository.save(passive);
        }catch (Exception e){
            return Mono.<Passive>error(new Error("The account number " + dataSavingAccount.getAccountNumber() + " do not exists"));
        }
    }

    @Override
    public Mono<Void> deleteSavingAccount(String accountNumber) {
        Mono<Passive> passiveMono = findSavingAccountByAccountNumber(accountNumber);
        try {
            return passiveRepository.delete(passiveMono.block());
        }catch (Exception e){
            return Mono.<Void>error(new Error("The account number " + accountNumber + " do not exists"));
        }
    }

}
