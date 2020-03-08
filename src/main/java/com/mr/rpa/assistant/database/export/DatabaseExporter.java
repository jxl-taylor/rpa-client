package com.mr.rpa.assistant.database.export;

import java.io.File;
import java.sql.CallableStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.mr.rpa.assistant.alert.AlertMaker;
import com.mr.rpa.assistant.database.DatabaseHandler;
import com.mr.rpa.assistant.util.AssistantUtil;
import javafx.concurrent.Task;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 *
 * @author Villan
 */
@Component
public class DatabaseExporter extends Task<Boolean> {

    private File backupDirectory;

    public void setBackupDirectory(File backupDirectory) {
        this.backupDirectory = backupDirectory;
    }

    @Resource
    private DatabaseHandler handler;


    @Override
    protected Boolean call() {
        try {
            createBackup();
            return true;
        } catch (Exception exp) {
            AlertMaker.showErrorMessage(exp);
        }
        return false;
    }

    private void createBackup() throws Exception {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy_MM_dd_hh_mm_ss");
        String backupdirectory = backupDirectory.getAbsolutePath() + File.separator + LocalDateTime.now().format(dateFormat);
        try (CallableStatement cs = handler.getConnection().prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)")) {
            cs.setString(1, backupdirectory);
            cs.execute();
        }
        File file = new File(backupdirectory);
        AssistantUtil.openFileWithDesktop(file);
    }
}
