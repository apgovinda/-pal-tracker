package io.pivotal.pal.tracker.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.pivotal.pal.tracker.model.TimeEntry;
import io.pivotal.pal.tracker.TimeEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/time-entries")
public class TimeEntryController {

    private TimeEntryRepository timeEntryRepository;
    private final DistributionSummary timeEntrySummary;
    private final Counter actionCounter;

    public TimeEntryController(TimeEntryRepository timeEntryRepository,
                               MeterRegistry meterRegistry) {
        this.timeEntryRepository = timeEntryRepository;

        timeEntrySummary = meterRegistry.summary("timeEntry.summary");
        actionCounter = meterRegistry.counter("timeEntry.actionCounter");

    }

    @PostMapping(produces= MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity create(@RequestBody TimeEntry timeEntryToCreate) {

       TimeEntry entry = timeEntryRepository.create(timeEntryToCreate);

       if(entry != null){
           actionCounter.increment();
           timeEntrySummary.record(timeEntryRepository.list().size());
           return  new ResponseEntity(entry, HttpStatus.CREATED);
       }
        return new ResponseEntity(entry, HttpStatus.BAD_REQUEST);

    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<TimeEntry> read(@PathVariable("id")long timeEntryId) {

        TimeEntry timeEntry = timeEntryRepository.find(timeEntryId);


        if(timeEntry != null){
            actionCounter.increment();
            return  new ResponseEntity<TimeEntry>(timeEntry, HttpStatus.OK);
        }
        return new ResponseEntity<TimeEntry>(timeEntry, HttpStatus.NOT_FOUND);

    }

    @GetMapping()
    public ResponseEntity<List<TimeEntry>> list() {
        List<TimeEntry> entries = timeEntryRepository.list();

        if(entries != null){
            actionCounter.increment();
            return new ResponseEntity<List<TimeEntry>>(entries, HttpStatus.OK);
        }
        return new ResponseEntity<List<TimeEntry>>(entries, HttpStatus.NOT_FOUND);

    }

    @PutMapping(value = "/{id}", produces=MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity update(@PathVariable("id")long timeEntryId, @RequestBody TimeEntry expected) {
        TimeEntry timeEntry = timeEntryRepository.update(timeEntryId,expected);

        if(timeEntry == null){
            return new ResponseEntity(timeEntry, HttpStatus.NOT_FOUND);
        }
        actionCounter.increment();
        return  new ResponseEntity(timeEntry, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<TimeEntry> delete(@PathVariable("id")long timeEntryId) {

        timeEntryRepository.delete(timeEntryId);
        actionCounter.increment();
        timeEntrySummary.record(timeEntryRepository.list().size());
        return  new ResponseEntity<TimeEntry>(HttpStatus.NO_CONTENT);
    }
}
