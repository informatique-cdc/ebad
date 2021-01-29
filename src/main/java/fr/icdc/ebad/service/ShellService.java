package fr.icdc.ebad.service;

import com.jcraft.jsch.Channel;
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
import fr.icdc.ebad.service.util.EbadServiceException;
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


    public RetourBatch runCommand(Environnement environnement, String command) throws EbadServiceException {
        LOGGER.debug("run command {}", command);
        Long start = System.currentTimeMillis();

        ChannelExec channelExec = null;
        try {
            channelExec = (ChannelExec) connect(ebadProperties.getSsh().getLogin(), environnement.getHost(), ebadProperties.getSsh().getPort(), SHELL);
            StringBuilder commandOut = new StringBuilder();

            String commandWithInterpreteur = environnement.getNorme().getCommandLine().replace("$1", command);
            channelExec = (ChannelExec) connect(ebadProperties.getSsh().getLogin(), environnement.getHost(), ebadProperties.getSsh().getPort(), SHELL, commandWithInterpreteur);

            try (InputStream in = channelExec.getInputStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    commandOut.append(line);
                }
            } catch (IOException e) {
                throw new EbadServiceException("Error when trying to get output from execution on the remote server", e);
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
            disconnect(channelExec);
        }
    }

    private static String constructSubDir(String originalSubDirectory) {
        String subDir = "";
        if (!StringUtils.isEmpty(originalSubDirectory)) {
            subDir = PATH_SEPARATOR + originalSubDirectory;
        }
        return subDir;
    }

    public List<ChannelSftp.LsEntry> getListFiles(Directory directory, String subDirectory) throws EbadServiceException {
        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) connect(ebadProperties.getSsh().getLogin(), directory.getEnvironnement().getHost(), ebadProperties.getSsh().getPort(), SFTP);
            String path = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory);
            LOGGER.debug("consultation du dossier {}", path);
            @SuppressWarnings("unchecked")
            List<ChannelSftp.LsEntry> lsEntries = channelSftp.ls(path);
            return lsEntries;
        } catch (SftpException e) {
            throw new EbadServiceException("Error when try to list files on the server", e);
        } finally {
            disconnect(channelSftp);
        }
    }

    public void removeFile(Directory directory, String filename, String subDirectory) throws EbadServiceException {
        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) connect(ebadProperties.getSsh().getLogin(), directory.getEnvironnement().getHost(), ebadProperties.getSsh().getPort(), SFTP);

            String path = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory) + PATH_SEPARATOR + filename;
            LOGGER.debug("suppression du fichier {}", path);
            channelSftp.rm(path);
        } catch (SftpException e) {
            throw new EbadServiceException("Error when try to remove file on the server", e);
        } finally {
            disconnect(channelSftp);
        }
    }

    public InputStream getFile(Directory directory, String filename, String subDirectory) throws EbadServiceException {

        ChannelSftp channelSftp = null;
        try {
            channelSftp = (ChannelSftp) connect(ebadProperties.getSsh().getLogin(), directory.getEnvironnement().getHost(), ebadProperties.getSsh().getPort(), SFTP);

            String srcPath = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory) + PATH_SEPARATOR + filename;
            LOGGER.debug("lecture du fichier {}", srcPath);
            FileDownloadProgressMonitor fileDownloadProgressMonitor = new FileDownloadProgressMonitor();

            try (InputStream inputStream = channelSftp.get(srcPath, fileDownloadProgressMonitor)) {
                File tmpFile = File.createTempFile("" + System.currentTimeMillis(), ".ebad-tmp");
                FileUtils.copyInputStreamToFile(inputStream, tmpFile);
                tmpFile.deleteOnExit();
                return new FileInputStream(tmpFile);

            } catch (IOException | SftpException e) {
                throw new EbadServiceException("Error when try to retrieve file on the server", e);
            }
        } finally {
            disconnect(channelSftp);
        }
    }

    private Channel connect(String login, String host, int port, String type) throws EbadServiceException {
        return connect(login, host, port, type, null);
    }

    private Channel connect(String login, String host, int port, String type, String command) throws EbadServiceException {
        Session session = null;
        try {
            session = jsch.getSession(login, host, port);
        } catch (JSchException e) {
            throw new EbadServiceException("Unable to connect to the server", e);
        }

        UserInfo ui = new SUserInfo(null, null);
        java.util.Properties config = new java.util.Properties();
        config.put(PROPERTY_CHECK_HOST, OPTION_NO);

        session.setConfig(config);
        session.setUserInfo(ui);
        try {
            session.connect();
            Channel channel = session.openChannel(type);
            if (type == SHELL && command != null) {
                ((ChannelExec) channel).setCommand(command);
            }
            channel.connect();
            return channel;
        } catch (JSchException e) {
            throw new EbadServiceException("Unable to connect to the server", e);
        }
    }

    private void disconnect(Channel channel) throws EbadServiceException {
        if (channel != null) {
            channel.disconnect();
            try {
                Session session = channel.getSession();
                if (session != null) {
                    session.disconnect();
                }
            } catch (JSchException e) {
                throw new EbadServiceException("Unable to disconnect properly to the server", e);
            }

        }
    }

    public void uploadFile(Directory directory, InputStream inputStream, String filename, String subDirectory) throws EbadServiceException {
        ChannelSftp channelSftp = (ChannelSftp) connect(ebadProperties.getSsh().getLogin(), directory.getEnvironnement().getHost(), ebadProperties.getSsh().getPort(), SFTP);
        String dstPath = directory.getEnvironnement().getHomePath() + PATH_SEPARATOR + directory.getPath() + constructSubDir(subDirectory) + PATH_SEPARATOR + filename;
        LOGGER.debug("écriture du fichier {}", dstPath);
        try {
            channelSftp.put(inputStream, dstPath);
        } catch (SftpException e) {
            throw new EbadServiceException("Error when send file to the server", e);
        }
        disconnect(channelSftp);
    }
}
