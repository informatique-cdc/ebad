package fr.icdc.ebad.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.service.util.FileDownloadProgressMonitor;
import fr.icdc.ebad.service.util.SUserInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by dtrouillet on 03/03/2016.
 */
@Service
public class ShellService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellService.class);
    private static final String SHELL = "exec";
    private static final String SFTP = "sftp";
    private static final String PROPERTY_CHECK_HOST = "StrictHostKeyChecking";
    private static final String OPTION_NO = "no";
    private static final int MILLIS_SLEEP = 1000;
    private static final String PATH_SEPARATOR = "/";

    private final EbadProperties ebadProperties;
    private final JSch jsch;

    public ShellService(EbadProperties ebadProperties, JSch jsch) {
        this.ebadProperties = ebadProperties;
        this.jsch = jsch;
    }


    public RetourBatch runCommand(Environnement environnement, String command) throws JSchException, IOException {
        LOGGER.debug("run command {}", command);
        Long start = System.currentTimeMillis();

        Session session = null;
        ChannelExec channelExec = null;
        try {
            session = jsch.getSession(ebadProperties.getSsh().getLogin(), environnement.getHost(), ebadProperties.getSsh().getPort());
            UserInfo ui = new SUserInfo("", null);// password = "" for unit test must try in integration if work

            java.util.Properties config = new java.util.Properties();
            config.put(PROPERTY_CHECK_HOST, OPTION_NO);

            session.setConfig(config);
            session.setUserInfo(ui);
            session.connect();

            channelExec = (ChannelExec) session.openChannel(SHELL);
            StringBuilder commandOut = new StringBuilder();
            try (InputStream in = channelExec.getInputStream()) {

                String commandWithInterpreteur = environnement.getNorme().getCommandLine().replace("$1", command);
                channelExec.setCommand(commandWithInterpreteur);
                channelExec.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    commandOut.append(line);
                }
            }

            int exitStatus = channelExec.getExitStatus();
            while (exitStatus == -1) {
                try {
                    Thread.sleep(MILLIS_SLEEP);
                } catch (InterruptedException e) {
                    LOGGER.error("Erreur lors de la reception du status", e);
                    Thread.currentThread().interrupt();
                }
                exitStatus = channelExec.getExitStatus();
            }


            Long end = System.currentTimeMillis();
            LOGGER.debug("Command out : {}", commandOut);
            return new RetourBatch(commandOut.toString(), exitStatus, end - start);
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    private static String constructSubDir(String originalSubDirectory) {
        String subDir = "";
        if (!StringUtils.isEmpty(originalSubDirectory)) {
            subDir = PATH_SEPARATOR + originalSubDirectory;
        }
        return subDir;
    }

    public List<ChannelSftp.LsEntry> getListFiles(Directory directory, String subDirectory) throws JSchException, SftpException {
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            session = jsch.getSession(ebadProperties.getSsh().getLogin(), directory.getEnvironnement().getHost(), ebadProperties.getSsh().getPort());
            UserInfo ui = new SUserInfo(null, null);
            java.util.Properties config = new java.util.Properties();
            config.put(PROPERTY_CHECK_HOST, OPTION_NO);

            session.setConfig(config);
            session.setUserInfo(ui);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel(SFTP);
            channelSftp.connect();

            String path = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory);
            LOGGER.debug("consultation du dossier {}", path);
            @SuppressWarnings("unchecked")
            List<ChannelSftp.LsEntry> lsEntries = channelSftp.ls(path);
            return lsEntries;
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    public void removeFile(Directory directory, String filename, String subDirectory) throws JSchException, SftpException {
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            session = jsch.getSession(ebadProperties.getSsh().getLogin(), directory.getEnvironnement().getHost(), ebadProperties.getSsh().getPort());
            UserInfo ui = new SUserInfo(null, null);
            java.util.Properties config = new java.util.Properties();
            config.put(PROPERTY_CHECK_HOST, OPTION_NO);

            session.setConfig(config);
            session.setUserInfo(ui);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel(SFTP);
            channelSftp.connect();
            String path = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory) + PATH_SEPARATOR + filename;
            LOGGER.debug("suppression du fichier {}", path);
            channelSftp.rm(path);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    public InputStream getFile(Directory directory, String filename, String subDirectory) throws JSchException, SftpException, IOException {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = jsch.getSession(ebadProperties.getSsh().getLogin(), directory.getEnvironnement().getHost(), ebadProperties.getSsh().getPort());
            UserInfo ui = new SUserInfo(null, null);
            java.util.Properties config = new java.util.Properties();
            config.put(PROPERTY_CHECK_HOST, OPTION_NO);

            session.setConfig(config);
            session.setUserInfo(ui);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel(SFTP);
            channelSftp.connect();

            String srcPath = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory) + PATH_SEPARATOR + filename;
            LOGGER.debug("lecture du fichier {}", srcPath);
            FileDownloadProgressMonitor fileDownloadProgressMonitor = new FileDownloadProgressMonitor();

            try (InputStream inputStream = channelSftp.get(srcPath, fileDownloadProgressMonitor)) {
                File tmpFile = File.createTempFile("" + System.currentTimeMillis(), ".ebad-tmp");
                FileUtils.copyInputStreamToFile(inputStream, tmpFile);
                tmpFile.deleteOnExit();
                return new FileInputStream(tmpFile);
            }
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    public void uploadFile(Directory directory, InputStream inputStream, String filename, String subDirectory) throws JSchException, SftpException {
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = jsch.getSession(ebadProperties.getSsh().getLogin(), directory.getEnvironnement().getHost(), ebadProperties.getSsh().getPort());

            UserInfo ui = new SUserInfo(null, null);
            java.util.Properties config = new java.util.Properties();
            config.put(PROPERTY_CHECK_HOST, OPTION_NO);

            session.setConfig(config);
            session.setUserInfo(ui);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel(SFTP);
            channelSftp.connect();

            String dstPath = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory) + PATH_SEPARATOR + filename;
            LOGGER.debug("écriture du fichier {}", dstPath);
            channelSftp.put(inputStream, dstPath);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
