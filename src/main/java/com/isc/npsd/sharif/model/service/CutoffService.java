package com.isc.npsd.sharif.model.service;

import com.isc.npsd.sharif.util.ParticipantUtil;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by A_Jankeh on 2/11/2017.
 */
@Stateless
@Local
public class CutoffService {

    @Inject
    private TrxService trxService;
    @Inject
    private STMTService stmtService;
    @Inject
    private BNPService bnpService;
    @Inject
    private MNPService mnpService;


    public void cutoff() {
        LocalTime startTime = LocalTime.now();
        stmtProcess();
        LocalTime endSTMP = LocalTime.now();
        Duration duration = Duration.between(startTime, endSTMP);
        System.out.println("STMT Duration : " + duration);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>");
        LocalTime startBNP = LocalTime.now();
        bnpProcess();
        LocalTime endTime = LocalTime.now();
        duration = Duration.between(startBNP, endTime);
        System.out.println("BNP Duration : " + duration);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>");

        Duration toatlDuration = Duration.between(startTime, endTime);
        System.out.println("Cutoff Duration : " + toatlDuration);
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    private void stmtProcess() {
        List<Future<String>> stmtFuture = new ArrayList<>();
        List<String> bics = ParticipantUtil.getInstance().getBics();
        bics.stream().forEach(creditorBIC -> stmtFuture.add(stmtService.saveTransactions(creditorBIC)));
        waitForFutures(stmtFuture);
    }

    private void bnpProcess() {
        List<Future<String>> bnpFuture = new ArrayList<>();
        List<String> bics = ParticipantUtil.getInstance().getBics();
        bics.stream().forEach(bic1 -> bnpFuture.add(mnpService.createBNPAndMNP(bic1)));
        waitForFutures(bnpFuture);
    }

    private void waitForFutures(List<Future<String>> futures) {
        futures.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

}
