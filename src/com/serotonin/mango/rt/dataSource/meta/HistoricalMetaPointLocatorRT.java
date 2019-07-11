package com.serotonin.mango.rt.dataSource.meta;

import java.util.HashMap;

import com.serotonin.db.IntValuePair;
import org.scada_lts.mango.service.ServiceInstances;
import com.serotonin.mango.rt.dataImage.DataPointRT;
import com.serotonin.mango.rt.dataImage.HistoricalDataPoint;
import com.serotonin.mango.rt.dataImage.IDataPoint;
import com.serotonin.mango.rt.dataImage.PointValueTime;
import com.serotonin.mango.vo.DataPointVO;
import com.serotonin.mango.vo.dataSource.meta.MetaPointLocatorVO;
import com.serotonin.timer.SimulationTimer;
import com.serotonin.web.i18n.LocalizableMessage;

public class HistoricalMetaPointLocatorRT extends MetaPointLocatorRT {
    private long updates;

    public HistoricalMetaPointLocatorRT(MetaPointLocatorVO vo) {
        super(vo);
    }

    public void initialize(SimulationTimer timer, DataPointRT dataPoint) {
        this.timer = timer;
        this.dataPoint = dataPoint;
        initialized = true;
        initializeTimerTask();

        context = new HashMap<String, IDataPoint>();
        for (IntValuePair contextEntry : vo.getContext()) {
            DataPointVO cvo = ServiceInstances.DataPointService.getDataPoint(contextEntry.getKey());
            HistoricalDataPoint point = new HistoricalDataPoint(cvo.getId(), cvo.getPointLocator().getDataTypeId(),
                    timer, ServiceInstances.PointValueService);
            context.put(contextEntry.getValue(), point);
        }
    }

    @Override
    public void terminate() {
        synchronized (LOCK) {
            // Cancel scheduled job
            if (timerTask != null)
                timerTask.cancel();
        }
    }

    public long getUpdates() {
        return updates;
    }

    @Override
    protected void updatePoint(PointValueTime pvt) {
        super.updatePoint(pvt);
        updates++;
    }

    @Override
    protected void handleError(long runtime, LocalizableMessage message) {
        throw new MetaPointExecutionException(message);
    }
}
