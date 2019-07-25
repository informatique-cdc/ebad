package fr.icdc.ebad.service.util;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

/**
 * Created by dtrouillet on 24/01/2017.
 */
public class FileDownloadProgressMonitor implements SftpProgressMonitor {
    private boolean end = false;
    private long size;
    private Session session;
    private ChannelSftp channelSftp;

    public FileDownloadProgressMonitor(){
    }

    public FileDownloadProgressMonitor(Session session, ChannelSftp channelSftp){
        this.session = session;
        this.channelSftp = channelSftp;
    }

    @Override
    public void init(int i, String s, String s1, long l) {
        size = l;
        end = false;
    }

    @Override
    public boolean count(long l) {
        size += l;
        return true;
    }

    @Override
    public void end() {
        if(channelSftp != null){
            channelSftp.disconnect();
        }

        if(session != null){
            session.disconnect();
        }
        end = true;
    }

    public boolean isEnded(){
        return end;
    }
}
