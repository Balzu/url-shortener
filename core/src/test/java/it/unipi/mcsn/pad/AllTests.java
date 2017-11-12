package it.unipi.mcsn.pad;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	DataReplicationTest.class,
	PrimaryFailureTest.class,
	ConflictResolutionTest.class})

public class AllTests {}
